package com.example.firstapp.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.example.firstapp.R
import com.example.firstapp.adapter.WeatherPagerAdapter
import com.example.firstapp.databinding.ActivityMainBinding
import com.example.firstapp.model.CityResponseApi
import com.example.firstapp.shared.PrefManager
import com.example.firstapp.shared.SharedData

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val prefManager by lazy { PrefManager(this) }
    private var adapter: WeatherPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        window.apply {
            statusBarColor = Color.TRANSPARENT
        }

        setupViewPager()

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

    // Hàm kiểm tra trùng lặp dựa trên tọa độ (chính xác hơn so với tên)
    private fun isSameLocation(city1: CityResponseApi.CityResponseItem, city2: CityResponseApi.CityResponseItem): Boolean {
        val threshold = 0.01 // Sai số nhỏ chấp nhận được
        val latDiff = Math.abs((city1.lat ?: 0.0) - (city2.lat ?: 0.0))
        val lonDiff = Math.abs((city1.lon ?: 0.0) - (city2.lon ?: 0.0))
        return latDiff < threshold && lonDiff < threshold
    }

    override fun onResume() {
        super.onResume()
        setupViewPager()
    }
}
