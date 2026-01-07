package com.example.firstapp.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.R
import com.example.firstapp.databinding.CityViewholderBinding
import com.example.firstapp.model.CityResponseApi
import com.example.firstapp.shared.PrefManager
import com.example.firstapp.shared.SharedData

class CityAdapter : RecyclerView.Adapter<CityAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CityViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = differ.currentList[position]
        val prefManager = PrefManager(holder.binding.root.context)
        
        val fullLocationName = buildString {
            append(city.name)
            if (!city.state.isNullOrEmpty()) append(", ${city.state}")
            if (!city.country.isNullOrEmpty()) append(", ${city.country}")
        }

        holder.binding.cityTxt.text = fullLocationName
        holder.binding.countryTxt.text = "Tọa độ: ${city.lat}, ${city.lon}"
        
        // Hiển thị trạng thái ngôi sao
        updateFavoriteIcon(holder, prefManager.isFavorite(city.name))

        // Xử lý khi nhấn vào ngôi sao (Thêm/Xóa yêu thích)
        holder.binding.favBtn.setOnClickListener {
            prefManager.toggleFavorite(city)
            updateFavoriteIcon(holder, prefManager.isFavorite(city.name))
        }

        // Xử lý khi nhấn vào cả dòng (Chỉ để xem, không tự động thêm vào yêu thích)
        holder.binding.root.setOnClickListener {
            SharedData.sharedLatitude = city.lat ?: 0.0
            SharedData.sharedLongitude = city.lon ?: 0.0
            SharedData.sharedCity = fullLocationName
            
            // Đánh dấu là đang xem một thành phố tạm thời (không phải trong list favorites mặc định)
            SharedData.isTemporarySearch = true
            
            (holder.binding.root.context as? Activity)?.finish()
        }
    }

    private fun updateFavoriteIcon(holder: ViewHolder, isFav: Boolean) {
        if (isFav) {
            holder.binding.favBtn.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            holder.binding.favBtn.setImageResource(android.R.drawable.btn_star_big_off)
        }
    }

    override fun getItemCount() = differ.currentList.size

    inner class ViewHolder(val binding: CityViewholderBinding) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<CityResponseApi.CityResponseItem>() {
        override fun areItemsTheSame(
            oldItem: CityResponseApi.CityResponseItem,
            newItem: CityResponseApi.CityResponseItem
        ): Boolean {
            return oldItem.lat == newItem.lat && oldItem.lon == newItem.lon
        }

        override fun areContentsTheSame(
            oldItem: CityResponseApi.CityResponseItem,
            newItem: CityResponseApi.CityResponseItem
        ): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)
}
