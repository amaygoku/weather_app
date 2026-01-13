package com.example.firstapp.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.adapter.ForecastAdapter
import com.example.firstapp.databinding.FragmentWeatherBinding
import com.example.firstapp.model.AirPollutionResponseApi
import com.example.firstapp.model.CurrentResponseApi
import com.example.firstapp.model.ForecastResponseApi
import com.example.firstapp.shared.SharedData
import com.example.firstapp.viewmodel.WeatherViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import eightbitlab.com.blurview.RenderScriptBlur

class WeatherFragment : Fragment() {
    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val forecastAdapter by lazy { ForecastAdapter() }

    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private var cityName: String = ""

    companion object {
        /**
         * Tạo instance mới của WeatherFragment với thông tin thành phố
         * @param lat Vĩ độ thành phố
         * @param lon Kinh độ thành phố
         * @param cityName Tên thành phố
         * @return WeatherFragment đã được khởi tạo
         */
        fun newInstance(lat: Double, lon: Double, cityName: String): WeatherFragment {
            val fragment = WeatherFragment()
            val args = Bundle()
            args.putDouble("lat", lat)
            args.putDouble("lon", lon)
            args.putString("cityName", cityName)
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * Khởi tạo Fragment và lấy các tham số đã truyền từ Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lat = it.getDouble("lat")
            lon = it.getDouble("lon")
            cityName = it.getString("cityName") ?: ""
        }
    }

    /**
     * Tạo view cho Fragment
     * @return View root của Fragment
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Gọi sau khi view đã được tạo
     * Thiết lập RecyclerView, Observers, Blur effect và tải dữ liệu thời tiết
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            forecastView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            forecastView.adapter = forecastAdapter

            setupObservers()
            setupChart()
            loadWeatherData()

            // Tối ưu Blur: Chỉ chạy sau khi UI đã ổn định
            view.post {
                try {
                    val radius = 10f
                    val decorView = requireActivity().window.decorView
                    val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
                    val windowBackground = decorView.background
                    
                    blurView.setupWith(rootView, RenderScriptBlur(requireContext()))
                        .setFrameClearDrawable(windowBackground)
                        .setBlurRadius(radius)
                    blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                    blurView.clipToOutline = true
                } catch (e: Exception) {
                    // Nếu Blur lỗi (do phần cứng máy ảo), ẩn BlurView để app không bị treo
                    blurView.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Tải tất cả dữ liệu thời tiết cho thành phố
     * Bao gồm: thời tiết hiện tại, dự báo và chất lượng không khí
     */
    private fun loadWeatherData() {
        val unit = SharedData.sharedUnit
        weatherViewModel.loadCurrentWeather(lat, lon, unit)
        weatherViewModel.loadForecastWeather(lat, lon, unit)
        weatherViewModel.loadAirPollution(lat, lon)
    }

    /**
     * Thiết lập các Observer để lắng nghe dữ liệu từ ViewModel
     * Cập nhật UI khi có dữ liệu mới
     */
    private fun setupObservers() {
        weatherViewModel.currentWeather.observe(viewLifecycleOwner, Observer { data ->
            updateWeatherUI(data)
        })

        weatherViewModel.forecastWeather.observe(viewLifecycleOwner, Observer { data ->
            updateForecastWeatherUI(data)
            updateChart(data)
        })

        weatherViewModel.airPollution.observe(viewLifecycleOwner, Observer { data ->
            updateAirUI(data)
        })
    }

    /**
     * Cập nhật hiển thị chỉ số chất lượng không khí (AQI)
     * @param data Dữ liệu AQI từ API
     */
    private fun updateAirUI(data: AirPollutionResponseApi) {
        val aqi = data.list?.get(0)?.main?.aqi ?: 0
        val aqiText = when (aqi) {
            1 -> getString(R.string.aqi_good)
            2 -> getString(R.string.aqi_fair)
            3 -> getString(R.string.aqi_moderate)
            4 -> getString(R.string.aqi_poor)
            5 -> getString(R.string.aqi_very_poor)
            else -> getString(R.string.aqi_unknown)
        }
        binding.aqiText.text = aqiText
    }

    /**
     * Thiết lập cấu hình cho biểu đồ nhiệt độ
     * Ẩn các thành phần không cần thiết
     */
    private fun setupChart() {
        binding.tempChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            xAxis.isEnabled = false
            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
        }
    }

    /**
     * Cập nhật biểu đồ nhiệt độ theo dữ liệu dự báo
     * Hiển thị nhiệt độ của 8 giờ tiếp theo
     * @param data Dữ liệu dự báo thời tiết
     */
    private fun updateChart(data: ForecastResponseApi) {
        val entries = ArrayList<Entry>()
        data.list?.take(8)?.forEachIndexed { index, forecast ->
            forecast?.main?.temp?.let {
                entries.add(Entry(index.toFloat(), it.toFloat()))
            }
        }

        val dataSet = LineDataSet(entries, "Nhiệt độ").apply {
            color = Color.WHITE
            setCircleColor(Color.WHITE)
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextColor = Color.WHITE
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.WHITE
            fillAlpha = 50
        }

        binding.tempChart.data = LineData(dataSet)
        binding.tempChart.invalidate()
    }

    /**
     * Chọn hình nền phù hợp với tình trạng thời tiết
     * @param icon Mã icon thời tiết từ API
     * @return Resource ID của hình nền
     */
    private fun setDynamicallyWallpaper(icon: String): Int {
        return when (icon.dropLast(1)) {
            "01" -> R.drawable.sunny_bg
            "02", "03", "04" -> R.drawable.cloudy_bg
            "09", "10", "11" -> R.drawable.rainy_bg
            "13" -> R.drawable.snow_bg
            "50" -> R.drawable.haze_bg
            else -> R.drawable.sunny_bg
        }
    }

    /**
     * Cập nhật giao diện hiển thị thông tin thời tiết hiện tại
     * Bao gồm: tên thành phố, nhiệt độ, độ ẩm, tốc độ gió, hình nền
     * @param data Dữ liệu thời tiết hiện tại
     */
    private fun updateWeatherUI(data: CurrentResponseApi) {
        val icon = data.weather?.get(0)?.icon ?: "-"
        // Dựa vào icon để biết trời đang sáng hay tối tại chính thành phố đó
        val isNight = icon.endsWith("n")

        binding.cityText.text = cityName
        binding.detailLayout.visibility = View.VISIBLE
        binding.statusText.text = data.weather?.get(0)?.main ?: "-"
        
        // Kiểm tra xem string resource có tồn tại không để tránh crash, nếu chưa có bạn hãy thêm vào strings.xml
        val windUnit = try { getString(R.string.wind_speed_unit) } catch (e: Exception) { "Km" }
        binding.windText.text = (data.wind?.speed?.let { Math.round(it).toString() } ?: "0") + " " + windUnit
        
        binding.humidityText.text = (data.main?.humidity?.toString() ?: "-") + "%"
        binding.currentTempText.text = (data.main?.temp?.let { Math.round(it).toString() } ?: "-") + "°"
        
        val maxTemp = data.main?.tempMax?.let { Math.round(it).toString() } ?: "0"
        val minTemp = data.main?.tempMin?.let { Math.round(it).toString() } ?: "0"
        
        val highText = try { getString(R.string.highest_temp_short) } catch (e: Exception) { "H" }
        val lowText = try { getString(R.string.lowest_temp_short) } catch (e: Exception) { "L" }
        binding.tempRangeText.text = "$highText:$maxTemp°  $lowText:$minTemp°"

        val drawable = if (isNight) {
            R.drawable.night_bg
        } else {
            setDynamicallyWallpaper(icon)
        }
        Glide.with(this).load(drawable).into(binding.backgroundImage)
    }

    /**
     * Cập nhật RecyclerView hiển thị danh sách dự báo thời tiết
     * @param data Dữ liệu dự báo thời tiết
     */
    private fun updateForecastWeatherUI(data: ForecastResponseApi) {
        binding.blurView.visibility = View.VISIBLE
        forecastAdapter.differ.submitList(data.list)
    }

    /**
     * Hủy view khi Fragment bị destroy
     * Giải phóng binding để tránh memory leak
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
