package com.example.firstapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.firstapp.fragment.WeatherFragment
import com.example.firstapp.model.CityResponseApi

class WeatherPagerAdapter(
    activity: FragmentActivity,
    private val favoriteCities: List<CityResponseApi.CityResponseItem>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = favoriteCities.size

    override fun createFragment(position: Int): Fragment {
        val city = favoriteCities[position]
        return WeatherFragment.newInstance(
            city.lat ?: 0.0,
            city.lon ?: 0.0,
            city.name ?: ""
        )
    }
}
