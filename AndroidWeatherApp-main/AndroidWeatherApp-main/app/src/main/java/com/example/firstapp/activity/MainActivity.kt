package com.example.firstapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.R
import com.example.firstapp.adapter.ForecastAdapter
import com.example.firstapp.databinding.ActivityMainBinding
import com.example.firstapp.model.CurrentResponseApi
import com.example.firstapp.model.ForecastResponseApi
import com.example.firstapp.shared.SharedData
import com.example.firstapp.viewmodel.WeatherViewModel
import com.google.gson.Gson
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val weatherViewModel:WeatherViewModel by viewModels()
    private val calendar by lazy {Calendar.getInstance()}

    private lateinit var detailLayout: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var windText: TextView
    private lateinit var humidityText: TextView
    private lateinit var currentTempText: TextView
    private lateinit var maxTempText: TextView
    private lateinit var minTempText: TextView
    private lateinit var backgroundImage: ImageView

    private lateinit var blurView: BlurView
    private lateinit var forecastView: RecyclerView
    private val forecastAdapter by lazy { ForecastAdapter() }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        detailLayout = findViewById(R.id.detailLayout)
        statusText = findViewById(R.id.statusText)
        windText = findViewById(R.id.windText)
        humidityText = findViewById(R.id.humidityText)
        currentTempText = findViewById(R.id.currentTempText)
        maxTempText = findViewById(R.id.maxTempText)
        minTempText = findViewById(R.id.minTempText)
        backgroundImage = findViewById(R.id.backgroundImage)

        // Initialize your views
        blurView = findViewById(R.id.blurView)
        forecastView = findViewById(R.id.forecastView)

        // Example: you might also set up any additional config here
        forecastView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        forecastView.adapter = forecastAdapter

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }

        binding.apply {

//            var lat = intent.getDoubleExtra("lat", 0.0)
//            var lon = intent.getDoubleExtra("lon", 0.0)
//            var name = intent.getStringExtra("name")
//
//            if(lat  == 0.0) {
//                lat = 21.03
//                lon = 0.0
//                name = "Hanoi"
//            }
            val lat: Double = SharedData.sharedLatitude
            val lon: Double = SharedData.sharedLongitude
            val name: String = SharedData.sharedCity
            val unit: String = SharedData.unitString



            // Log the values
            Log.d("LocationInfo", "Latitude: $lat, Longitude: $lon, Name: $name")
            addCityButton.setOnClickListener{
                startActivity(Intent(this@MainActivity, AddCityActivity::class.java))
            }

            settingButton.setOnClickListener{
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }


            // Current Temp
            cityText.text = name
            if (isNetworkAvailable(this@MainActivity)) {
                // We have an internet connection, so fetch from API
                fetchWeatherDataFromApi(lat, lon, unit)
            } else {
                // No internet, load from local JSON
                fetchWeatherDataFromApi(lat, lon, unit)
            }

            // Setting Blur View
            val radius = 10f
            val decorView = window.decorView
            val rootView: ViewGroup = (decorView.findViewById(android.R.id.content))
            val windowBackground = (decorView.background)
            rootView.let{
                blurView.setupWith(it, RenderScriptBlur(this@MainActivity))
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius)
                blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                blurView.clipToOutline = true
            }


            // Forecast temperature
            fetchForecastWeatherDataFromApi(lat, lon, unit)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun isNightNow() : Boolean{
        return calendar.get(Calendar.HOUR_OF_DAY) >= 18
    }
    private fun setDynamicallyWallpaper(icon:String):Int{
        return when(icon.dropLast(1)){
            "01" -> {
                R.drawable.snow_bg
            }
            "02", "03", "04" ->{
                R.drawable.cloudy_bg
            }
            "09", "10", "11" ->{
                R.drawable.rainy_bg
            }
            "13"->{
                R.drawable.snow_bg
            }
            "50"->{
                R.drawable.haze_bg
            }
            else -> 0
        }
    }
    private fun fetchWeatherDataFromApi(lat: Double, lon: Double, unit: String) {
        // This calls the Retrofit service in your ViewModel
        weatherViewModel.loadCurrentWeather(lat, lon, unit).enqueue(object : Callback<CurrentResponseApi> {
            override fun onResponse(
                call: Call<CurrentResponseApi>,
                response: Response<CurrentResponseApi>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // TODO: Update UI with `data`
                    updateWeatherUI(data)

                    // Save the data to JSON
                    saveWeatherDataToJson(data)
                } else {
                    val cachedData = loadWeatherDataFromLocalJson()
                    if (cachedData != null) {
                        // Update UI with cached data
                    } else {
                        // If no cached data is available, show an error message
                        //Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<CurrentResponseApi>, t: Throwable) {
                // Handle failure
                val cachedData = loadWeatherDataFromLocalJson()
                if (cachedData != null) {
                    // Update UI with cached data
                } else {
                    // If no cached data is available, show an error message
                    Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun loadWeatherDataFromLocalJson() {
        try {
            val fileInputStream = openFileInput("current_weather.json")
            val inputStreamReader = InputStreamReader(fileInputStream)
            val jsonString = inputStreamReader.readText()
            inputStreamReader.close()
            fileInputStream.close()

            // Parse JSON
            val gson = Gson()
            val data = gson.fromJson(jsonString, CurrentResponseApi::class.java)

            // TODO: Update UI with data
            updateWeatherUI(data)
        } catch (e: Exception) {
            e.printStackTrace()
            // Show error message or fallback if file not found
        }
    }

    private fun saveWeatherDataToJson(data: CurrentResponseApi) {
        try {
            // Convert object to JSON
            val gson = Gson()
            val jsonString = gson.toJson(data)

            // Write JSON to a file in internal storage
            val fileOutputStream = openFileOutput("current_weather.json", MODE_PRIVATE)
            fileOutputStream.write(jsonString.toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun updateWeatherUI(data: CurrentResponseApi) {
        // Now updateWeatherUI will see these class-level properties
        detailLayout.visibility = View.VISIBLE

        statusText.text = data.weather?.get(0)?.main ?: "-"
        windText.text = (data.wind?.speed?.let { speed -> Math.round(speed).toString() } ?: "0") + " Km"
        humidityText.text = (data.main?.humidity?.toString() ?: "-") + "%"
        currentTempText.text = (data.main?.temp?.let { temp -> Math.round(temp).toString() } ?: "-") + "°"
        maxTempText.text = (data.main?.tempMax?.let { temp -> Math.round(temp).toString() } ?: "-") + "°"
        minTempText.text = (data.main?.tempMin?.let { temp -> Math.round(temp).toString() } ?: "-") + "°"

        // Dynamically set the background
        val drawable = if (isNightNow()) {
            R.drawable.night_bg
        } else {
            // If setDynamicallyWallpaper returns an Int (drawable resource ID), that’s what you use
            setDynamicallyWallpaper(data.weather?.get(0)?.icon ?: "-")
        }
        backgroundImage.setImageResource(drawable)
    }
    private fun fetchForecastWeatherDataFromApi(lat: Double, lon: Double, unit: String) {
        // This calls the Retrofit service in your ViewModel
        weatherViewModel. loadForecastWeather(lat, lon, unit).enqueue(object : Callback<ForecastResponseApi> {
            override fun onResponse(
                call: Call<ForecastResponseApi>,
                response: Response<ForecastResponseApi>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // TODO: Update UI with `data`
                    updateForecastWeatherUI(data)

                    // Save the data to JSON
                    saveForeCastWeatherDataToJson(data)
                } else {
                    val cachedData = loadForecastWeatherDataFromLocalJson()
                }
            }

            override fun onFailure(call: Call<ForecastResponseApi>, t: Throwable) {
                // Handle failure
                val cachedData = loadForecastWeatherDataFromLocalJson()
                if (cachedData != null) {
                    // Update UI with cached data
                } else {
                    // If no cached data is available, show an error message
                    Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun loadForecastWeatherDataFromLocalJson() {
        try {
            val fileInputStream = openFileInput("forecast_weather.json")
            val inputStreamReader = InputStreamReader(fileInputStream)
            val jsonString = inputStreamReader.readText()
            inputStreamReader.close()
            fileInputStream.close()

            // Parse JSON
            val gson = Gson()
            val data = gson.fromJson(jsonString, ForecastResponseApi::class.java)

            // TODO: Update UI with data
            updateForecastWeatherUI(data)
        } catch (e: Exception) {
            e.printStackTrace()
            // Show error message or fallback if file not found
        }
    }

    private fun saveForeCastWeatherDataToJson(data: ForecastResponseApi) {
        try {
            // Convert object to JSON
            val gson = Gson()
            val jsonString = gson.toJson(data)

            // Write JSON to a file in internal storage
            val fileOutputStream = openFileOutput("forecast_weather.json", MODE_PRIVATE)
            fileOutputStream.write(jsonString.toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun updateForecastWeatherUI(data: ForecastResponseApi) {
        // Show the blurView (or any other UI changes you want)
        blurView.visibility = View.VISIBLE

        // Submit new forecast list to your adapter
        forecastAdapter.differ.submitList(data.list)

        // If you want to ensure the layout manager is still correct, you can do it here too:
        forecastView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        forecastView.adapter = forecastAdapter
    }
}