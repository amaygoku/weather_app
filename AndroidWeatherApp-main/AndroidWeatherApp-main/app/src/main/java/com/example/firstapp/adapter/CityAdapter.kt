package com.example.firstapp.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.databinding.CityViewholderBinding
import com.example.firstapp.model.CityResponseApi
import com.example.firstapp.shared.PrefManager
import com.example.firstapp.shared.SharedData

class CityAdapter : RecyclerView.Adapter<CityAdapter.ViewHolder>() {

    /**
     * Tạo ViewHolder mới cho mỗi item trong RecyclerView
     * @param parent ViewGroup chứa ViewHolder
     * @param viewType Loại view
     * @return ViewHolder đã được tạo
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CityViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * Gắn dữ liệu thành phố vào ViewHolder
     * Xử lý sự kiện click vào thành phố và nút yêu thích
     * @param holder ViewHolder cần bind dữ liệu
     * @param position Vị trí của item trong danh sách
     */
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
        
        // Cập nhật trạng thái ngôi sao chính xác theo tọa độ
        updateFavoriteIcon(holder, prefManager.isFavorite(city))

        holder.binding.favBtn.setOnClickListener {
            prefManager.toggleFavorite(city)
            updateFavoriteIcon(holder, prefManager.isFavorite(city))
        }

        holder.binding.root.setOnClickListener {
            SharedData.sharedLatitude = city.lat ?: 0.0
            SharedData.sharedLongitude = city.lon ?: 0.0
            SharedData.sharedCity = fullLocationName
            SharedData.isTemporarySearch = true
            (holder.binding.root.context as? Activity)?.finish()
        }
    }

    /**
     * Cập nhật icon ngôi sao yêu thích
     * @param holder ViewHolder chứa icon
     * @param isFav true nếu thành phố đã được yêu thích, false nếu chưa
     */
    private fun updateFavoriteIcon(holder: ViewHolder, isFav: Boolean) {
        if (isFav) {
            holder.binding.favBtn.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            holder.binding.favBtn.setImageResource(android.R.drawable.btn_star_big_off)
        }
    }

    /**
     * Lấy số lượng item trong RecyclerView
     * @return Số lượng thành phố trong danh sách
     */
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
