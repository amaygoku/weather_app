package com.example.firstapp.fragment

import android.graphics.Color
import android.icu.util.Calendar
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
    private val calendar by lazy { Calendar.getInstance() }
    private val forecastAdapter by lazy { ForecastAdapter() }

    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private var cityName: String = ""

    companion object {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lat = it.getDouble("lat")
            lon = it.getDouble("lon")
            cityName = it.getString("cityName") ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    private fun loadWeatherData() {
        val unit = SharedData.sharedUnit
        weatherViewModel.loadCurrentWeather(lat, lon, unit)
        weatherViewModel.loadForecastWeather(lat, lon, unit)
        weatherViewModel.loadAirPollution(lat, lon)
    }

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

    private fun updateAirUI(data: AirPollutionResponseApi) {
        val aqi = data.list?.get(0)?.main?.aqi ?: 0
        val aqiText = when (aqi) {
            1 -> "AQI: Tốt"
            2 -> "AQI: Khá"
            3 -> "AQI: Trung bình"
            4 -> "AQI: Kém"
            5 -> "AQI: Rất kém"
            else -> "AQI: -"
        }
        binding.aqiText.text = aqiText
    }

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

    private fun isNightNow(): Boolean {
        return calendar.get(Calendar.HOUR_OF_DAY) >= 18
    }

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

    private fun updateWeatherUI(data: CurrentResponseApi) {
        binding.cityText.text = cityName
        binding.detailLayout.visibility = View.VISIBLE
        binding.statusText.text = data.weather?.get(0)?.main ?: "-"
        binding.windText.text = (data.wind?.speed?.let { Math.round(it).toString() } ?: "0") + " Km"
        binding.humidityText.text = (data.main?.humidity?.toString() ?: "-") + "%"
        binding.currentTempText.text = (data.main?.temp?.let { Math.round(it).toString() } ?: "-") + "°"
        
        val maxTemp = data.main?.tempMax?.let { Math.round(it).toString() } ?: "0"
        val minTemp = data.main?.tempMin?.let { Math.round(it).toString() } ?: "0"
        binding.tempRangeText.text = "H:$maxTemp°  L:$minTemp°"

        val drawable = if (isNightNow()) {
            R.drawable.night_bg
        } else {
            setDynamicallyWallpaper(data.weather?.get(0)?.icon ?: "-")
        }
        Glide.with(this).load(drawable).into(binding.backgroundImage)
    }

    private fun updateForecastWeatherUI(data: ForecastResponseApi) {
        binding.blurView.visibility = View.VISIBLE
        forecastAdapter.differ.submitList(data.list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
