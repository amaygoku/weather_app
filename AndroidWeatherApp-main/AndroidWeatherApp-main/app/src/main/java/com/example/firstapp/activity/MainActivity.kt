package com.example.firstapp.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.firstapp.R
import com.example.firstapp.adapter.WeatherPagerAdapter
import com.example.firstapp.databinding.ActivityMainBinding
import com.example.firstapp.model.CityResponseApi
import com.example.firstapp.shared.PrefManager
import com.example.firstapp.shared.SharedData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val prefManager by lazy { PrefManager(this) }
    private var adapter: WeatherPagerAdapter? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                getCurrentLocation()
            }
            else -> {
                Toast.makeText(this, getString(R.string.location_permission_required), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Khởi tạo Activity khi màn hình được tạo
     * Áp dụng ngôn ngữ đã lưu, thiết lập giao diện và các listener
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved language before setting content view
        val savedLanguage = prefManager.getLanguage()
        setLocale(savedLanguage)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        window.apply {
            statusBarColor = Color.TRANSPARENT
        }
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupViewPager()

        binding.gpsButton.setOnClickListener {
            checkLocationPermissionAndGetLocation()
        }

        binding.addCityButton.setOnClickListener {
            startActivity(Intent(this, AddCityActivity::class.java))
        }

        binding.settingButton.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Thiết lập ViewPager để hiển thị danh sách các thành phố
     * Bao gồm: thành phố mặc định (Hà Nội), thành phố tìm kiếm tạm thời và thành phố yêu thích
     */
    private fun setupViewPager() {
        val finalCities = mutableListOf<CityResponseApi.CityResponseItem>()
        
        // 1. Thành phố Hà Nội mặc định
        val hanoi = CityResponseApi.CityResponseItem(
            country = "VN", lat = 21.0285, lon = 105.8542, name = "Hanoi", state = null, localNames = null
        )

        // 2. Nếu có tìm kiếm tạm thời, đưa lên đầu trang
        if (SharedData.isTemporarySearch) {
            val searchedCity = CityResponseApi.CityResponseItem(
                name = SharedData.sharedCity,
                lat = SharedData.sharedLatitude,
                lon = SharedData.sharedLongitude,
                country = null, state = null, localNames = null
            )
            finalCities.add(searchedCity)
        }

        // 3. Thêm Hà Nội vào nếu chưa có trong danh sách (so sánh theo tọa độ)
        if (finalCities.none { isSameLocation(it, hanoi) }) {
            finalCities.add(hanoi)
        }

        // 4. Thêm các thành phố yêu thích từ máy (lọc trùng theo tọa độ)
        val favorites = prefManager.getFavorites()
        favorites.forEach { fav ->
            if (finalCities.none { isSameLocation(it, fav) }) {
                finalCities.add(fav)
            }
        }

        adapter = WeatherPagerAdapter(this, finalCities)
        binding.viewPager.adapter = adapter
        
        // Nếu vừa tìm kiếm, chuyển sang trang đầu tiên (thành phố vừa tìm)
        if (SharedData.isTemporarySearch) {
            binding.viewPager.setCurrentItem(0, false)
            SharedData.isTemporarySearch = false 
        }
    }

    /**
     * Kiểm tra hai thành phố có cùng vị trí địa lý hay không
     * So sánh dựa trên tọa độ (latitude, longitude) với sai số cho phép 0.01
     * @param city1 Thành phố thứ nhất
     * @param city2 Thành phố thứ hai
     * @return true nếu hai thành phố cùng vị trí, false nếu khác
     */
    private fun isSameLocation(city1: CityResponseApi.CityResponseItem, city2: CityResponseApi.CityResponseItem): Boolean {
    /**
     * Được gọi khi Activity quay lại foreground
     * Cập nhật lại ViewPager để hiển thị các thay đổi mới (thêm/xóa thành phố)
     */
        val threshold = 0.01 // Sai số nhỏ chấp nhận được
        val latDiff = Math.abs((city1.lat ?: 0.0) - (city2.lat ?: 0.0))
        val lonDiff = Math.abs((city1.lon ?: 0.0) - (city2.lon ?: 0.0))
        return latDiff < threshold && lonDiff < threshold
    }

    override fun onResume() {
    /**
     * Kiểm tra quyền truy cập vị trí của ứng dụng
     * Nếu đã có quyền: lấy vị trí hiện tại
     * Nếu chưa có: yêu cầu người dùng cấp quyền
     */
        super.onResume()
        setupViewPager()
    }
    
    private fun checkLocationPermissionAndGetLocation() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
    /**
     * Lấy vị trí GPS hiện tại của thiết bị
     * Sử dụng Geocoder để chuyển đổi tọa độ thành tên thành phố
     * Cập nhật SharedData và làm mới ViewPager để hiển thị thành phố hiện tại
     */
                )
            }
        }
    }
    
    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        Toast.makeText(this, getString(R.string.getting_location), Toast.LENGTH_SHORT).show()
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                
                // Reverse geocoding để lấy tên thành phố
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    
                    val cityName = addresses?.firstOrNull()?.let { address ->
                        address.locality ?: address.subAdminArea ?: address.adminArea ?: getString(R.string.current_location)
                    } ?: getString(R.string.current_location)
                    
                    // Cập nhật SharedData để WeatherFragment hiển thị
                    SharedData.sharedLatitude = lat
                    SharedData.sharedLongitude = lon
                    SharedData.sharedCity = cityName
                    SharedData.isTemporarySearch = true
                    
                    Toast.makeText(this, getString(R.string.got_location, cityName), Toast.LENGTH_SHORT).show()
                    
                    // Refresh ViewPager để hiển thị thành phố vừa detect
                    setupViewPager()
                    
                } catch (e: Exception) {
                    Toast.makeText(this, getString(R.string.geocoder_error, e.message), Toast.LENGTH_SHORT).show()
                    
                    // Vẫn hiển thị thời tiết dù không có tên thành phố
                    SharedData.sharedLatitude = lat
                    SharedData.sharedLongitude = lon
                    SharedData.sharedCity = "Lat: $lat, Lon: $lon"
                    SharedData.isTemporarySearch = true
    /**
     * Thay đổi ngôn ngữ hiển thị của ứng dụng
     * @param languageCode Mã ngôn ngữ ("vi" cho Tiếng Việt, "en" cho English)
     */
                    setupViewPager()
                }
            } else {
                Toast.makeText(this, getString(R.string.location_failed), Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, getString(R.string.gps_error, e.message), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
