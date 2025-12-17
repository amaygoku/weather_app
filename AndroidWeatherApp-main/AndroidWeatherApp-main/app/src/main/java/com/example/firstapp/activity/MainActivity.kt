package com.example.firstapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.adapter.ForecastAdapter
import com.example.firstapp.databinding.ActivityMainBinding
import com.example.firstapp.model.CurrentResponseApi
import com.example.firstapp.model.ForecastResponseApi
import com.example.firstapp.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import eightbitlab.com.blurview.RenderScriptBlur

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val calendar by lazy { Calendar.getInstance() }
    private val forecastAdapter by lazy { ForecastAdapter() }
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            getLastLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }

        binding.apply {
            forecastView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            forecastView.adapter = forecastAdapter

            addCityButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, AddCityActivity::class.java))
            }

            settingButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }

            requestLocationPermissions()

            setupObservers()

            val radius = 10f
            val decorView = window.decorView
            val rootView: ViewGroup = decorView.findViewById(android.R.id.content)
            val windowBackground = decorView.background
            rootView.let {
                blurView.setupWith(it, RenderScriptBlur(this@MainActivity))
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius)
                blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                blurView.clipToOutline = true
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun requestLocationPermissions() {
        when {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    // Fetch weather data with the new location
                    weatherViewModel.loadCurrentWeather(lat, lon, "metric")
                    weatherViewModel.loadForecastWeather(lat, lon, "metric")
                } else {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupObservers() {
        weatherViewModel.currentWeather.observe(this, Observer { data ->
            updateWeatherUI(data)
        })

        weatherViewModel.forecastWeather.observe(this, Observer { data ->
            updateForecastWeatherUI(data)
        })

        weatherViewModel.error.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        })
    }

    private fun isNightNow(): Boolean {
        return calendar.get(Calendar.HOUR_OF_DAY) >= 18
    }

    private fun setDynamicallyWallpaper(icon: String): Int {
        return when (icon.dropLast(1)) {
            "01" -> R.drawable.snow_bg
            "02", "03", "04" -> R.drawable.cloudy_bg
            "09", "10", "11" -> R.drawable.rainy_bg
            "13" -> R.drawable.snow_bg
            "50" -> R.drawable.haze_bg
            else -> 0
        }
    }

    private fun updateWeatherUI(data: CurrentResponseApi) {
        binding.cityText.text = data.name
        binding.detailLayout.visibility = View.VISIBLE
        binding.statusText.text = data.weather?.get(0)?.main ?: "-"
        binding.windText.text = (data.wind?.speed?.let { speed -> Math.round(speed).toString() } ?: "0") + " Km"
        binding.humidityText.text = (data.main?.humidity?.toString() ?: "-") + "%"
        binding.currentTempText.text = (data.main?.temp?.let { temp -> Math.round(temp).toString() } ?: "-") + "°"
        binding.maxTempText.text = (data.main?.tempMax?.let { temp -> Math.round(temp).toString() } ?: "-") + "°"
        binding.minTempText.text = (data.main?.tempMin?.let { temp -> Math.round(temp).toString() } ?: "-") + "°"

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
}
