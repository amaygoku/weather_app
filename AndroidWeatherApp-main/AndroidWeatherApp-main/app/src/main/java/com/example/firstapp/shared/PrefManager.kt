package com.example.firstapp.shared

import android.content.Context
import android.content.SharedPreferences
import com.example.firstapp.model.CityResponseApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PrefManager(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences("WeatherPref", Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Lưu vị trí thành phố vào SharedPreferences
     * Tự động thêm vào lịch sử tìm kiếm
     * @param lat Vĩ độ
     * @param lon Kinh độ
     * @param cityName Tên thành phố
     */
    fun saveLocation(lat: Double, lon: Double, cityName: String) {
        pref.edit().apply {
            putFloat("lat", lat.toFloat())
            putFloat("lon", lon.toFloat())
            putString("city", cityName)
            apply()
        }
        addCityToHistory(cityName, lat, lon)
    }

    /**
     * Lấy danh sách các thành phố yêu thích từ SharedPreferences
     * @return Danh sách thành phố yêu thích
     */
    fun getFavorites(): MutableList<CityResponseApi.CityResponseItem> {
        val json = pref.getString("favorites", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<CityResponseApi.CityResponseItem>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Thêm hoặc xóa thành phố khỏi danh sách yêu thích
     * Nếu thành phố đã có: xóa
     * Nếu thành phố chưa có: thêm vào
     * @param city Thành phố cần toggle
     */
    fun toggleFavorite(city: CityResponseApi.CityResponseItem) {
        val favorites = getFavorites()
        val exists = favorites.find { it.name == city.name && it.lat == city.lat && it.lon == city.lon }
        if (exists != null) {
            favorites.removeAll { it.name == city.name && it.lat == city.lat && it.lon == city.lon }
        } else {
            favorites.add(city)
        }
        pref.edit().putString("favorites", gson.toJson(favorites)).apply()
    }

    /**
     * Kiểm tra thành phố có trong danh sách yêu thích hay không
     * So sánh dựa trên tên và tọa độ
     * @param city Thành phố cần kiểm tra
     * @return true nếu thành phố đã được yêu thích, false nếu chưa
     */
    fun isFavorite(city: CityResponseApi.CityResponseItem): Boolean {
        return getFavorites().any { it.name == city.name && it.lat == city.lat && it.lon == city.lon }
    }

    /**
     * Thêm thành phố vào lịch sử tìm kiếm
     * Lưu tối đa 5 thành phố gần nhất
     * @param name Tên thành phố
     * @param lat Vĩ độ
     * @param lon Kinh độ
     */
    private fun addCityToHistory(name: String, lat: Double, lon: Double) {
        val history = getHistory().toMutableList()
        history.removeAll { it.name == name }
        history.add(0, CityResponseApi.CityResponseItem(null, lat, null, lon, name, null))
        val limitedHistory = if (history.size > 5) history.take(5) else history
        pref.edit().putString("search_history", gson.toJson(limitedHistory)).apply()
    }

    /**
     * Lấy lịch sử tìm kiếm các thành phố
     * @return Danh sách thành phố đã tìm gần đây
     */
    fun getHistory(): List<CityResponseApi.CityResponseItem> {
        val json = pref.getString("search_history", null) ?: return emptyList()
        val type = object : TypeToken<List<CityResponseApi.CityResponseItem>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Tải dữ liệu đã lưu vào SharedData
     * Bao gồm: vị trí, tên thành phố, đơn vị nhiệt độ
     */
    fun loadData() {
        SharedData.sharedLatitude = pref.getFloat("lat", 21.0285f).toDouble()
        SharedData.sharedLongitude = pref.getFloat("lon", 105.8542f).toDouble()
        SharedData.sharedCity = pref.getString("city", "Hanoi") ?: "Hanoi"
        SharedData.sharedUnit = pref.getString("unit", "metric") ?: "metric"
    }

    /**
     * Lưu đơn vị nhiệt độ vào SharedPreferences
     * @param unit Đơn vị nhiệt độ ("metric" hoặc "imperial")
     */
    fun saveUnit(unit: String) {
        pref.edit().putString("unit", unit).apply()
    }
    
    /**
     * Lưu ngôn ngữ hiển thị vào SharedPreferences
     * @param languageCode Mã ngôn ngữ ("vi" hoặc "en")
     */
    fun saveLanguage(languageCode: String) {
        pref.edit().putString("language", languageCode).apply()
    }
    
    /**
     * Lấy ngôn ngữ hiển tại từ SharedPreferences
     * @return Mã ngôn ngữ hiện tại (mặc định: "en")
     */
    fun getLanguage(): String {
        return pref.getString("language", "en") ?: "en"
    }
}
