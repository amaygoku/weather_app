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

    /**
     * Lấy số lượng trang (số thành phố) trong ViewPager
     * @return Số lượng thành phố
     */
    override fun getItemCount(): Int = favoriteCities.size

    /**
     * Tạo Fragment hiển thị thời tiết cho mỗi thành phố
     * @param position Vị trí của thành phố trong danh sách
     * @return WeatherFragment chứa thông tin thời tiết của thành phố
     */
    override fun createFragment(position: Int): Fragment {
        val city = favoriteCities[position]
        return WeatherFragment.newInstance(
            city.lat ?: 0.0,
            city.lon ?: 0.0,
            city.name ?: ""
        )
    }
}
