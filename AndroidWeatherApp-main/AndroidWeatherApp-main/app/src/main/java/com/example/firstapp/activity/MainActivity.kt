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
    private var currentCityList = mutableListOf<CityResponseApi.CityResponseItem>()

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
        val newList = mutableListOf<CityResponseApi.CityResponseItem>()
        val hanoi = CityResponseApi.CityResponseItem(
            country = "VN", lat = 21.0285, lon = 105.8542, name = "Hanoi", state = null, localNames = null
        )

        if (SharedData.isTemporarySearch) {
            newList.add(CityResponseApi.CityResponseItem(
                name = SharedData.sharedCity, lat = SharedData.sharedLatitude, lon = SharedData.sharedLongitude,
                country = null, state = null, localNames = null
            ))
            SharedData.isTemporarySearch = false
        }

        if (newList.none { isSameLocation(it, hanoi) }) newList.add(hanoi)
        
        prefManager.getFavorites().forEach { fav ->
            if (newList.none { isSameLocation(it, fav) }) newList.add(fav)
        }

        // Tối ưu: Chỉ cập nhật adapter nếu danh sách thành phố có sự thay đổi
        if (adapter == null || newList != currentCityList) {
            currentCityList = newList
            adapter = WeatherPagerAdapter(this, currentCityList)
            binding.viewPager.adapter = adapter
        }
    }

    private fun isSameLocation(city1: CityResponseApi.CityResponseItem, city2: CityResponseApi.CityResponseItem): Boolean {
        val threshold = 0.01 
        return Math.abs((city1.lat ?: 0.0) - (city2.lat ?: 0.0)) < threshold && 
               Math.abs((city1.lon ?: 0.0) - (city2.lon ?: 0.0)) < threshold
    }

    override fun onResume() {
        super.onResume()
        setupViewPager()
    }
}
