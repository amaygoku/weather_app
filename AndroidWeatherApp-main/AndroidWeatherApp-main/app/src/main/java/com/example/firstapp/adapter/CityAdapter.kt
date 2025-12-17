package com.example.firstapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.activity.MainActivity
import com.example.firstapp.databinding.CityViewholderBinding
import com.example.firstapp.model.CityResponseApi
import com.example.firstapp.shared.SharedData

class CityAdapter : RecyclerView.Adapter<CityAdapter.ViewHolder>() {
    private lateinit var binding: CityViewholderBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = CityViewholderBinding.inflate(inflater, parent, false)
        return ViewHolder()
    }
    inner class ViewHolder:RecyclerView.ViewHolder(binding.root)

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = CityViewholderBinding.bind(holder.itemView)
        binding.cityTxt.text = differ.currentList[position].name
        binding.root.setOnClickListener{
            val intent = Intent(binding.root.context, MainActivity::class.java)
//            intent.putExtra("lat",differ.currentList[position].lat)
//            intent.putExtra("lng",differ.currentList[position].lon)
//            intent.putExtra("name",differ.currentList[position].name)
            SharedData.sharedLatitude = differ.currentList[position].lat!!
            SharedData.sharedLongitude = differ.currentList[position].lon!!
            SharedData.sharedCity = differ.currentList[position].name.toString()
            binding.root.context.startActivity(intent)
        }

    }

    private val differCallback = object :DiffUtil.ItemCallback<CityResponseApi.CityResponseItem>(){
        override fun areItemsTheSame(
            oldItem: CityResponseApi.CityResponseItem,
            newItem: CityResponseApi.CityResponseItem
        ): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(
            oldItem: CityResponseApi.CityResponseItem,
            newItem: CityResponseApi.CityResponseItem
        ): Boolean {
            return oldItem==newItem
        }

    }
    val differ = AsyncListDiffer(this, differCallback)


}