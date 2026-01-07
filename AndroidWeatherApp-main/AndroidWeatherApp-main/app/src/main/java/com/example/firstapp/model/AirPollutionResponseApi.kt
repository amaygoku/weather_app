package com.example.firstapp.model

import com.google.gson.annotations.SerializedName

data class AirPollutionResponseApi(
    @SerializedName("coord")
    val coord: Coord?,
    @SerializedName("list")
    val list: List<AirItem?>?
) {
    data class Coord(
        @SerializedName("lat") val lat: Double?,
        @SerializedName("lon") val lon: Double?
    )

    data class AirItem(
        @SerializedName("main")
        val main: Main?,
        @SerializedName("components")
        val components: Map<String, Double>?,
        @SerializedName("dt")
        val dt: Int?
    )

    data class Main(
        @SerializedName("aqi")
        val aqi: Int? // 1 = Good, 2 = Fair, 3 = Moderate, 4 = Poor, 5 = Very Poor
    )
}
