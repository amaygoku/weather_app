package com.example.firstapp.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.R
import com.example.firstapp.adapter.CityAdapter
import com.example.firstapp.databinding.ActivityAddCityBinding
import com.example.firstapp.model.CityResponseApi
import com.example.firstapp.viewmodel.CityViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddCityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCityBinding
    private val cityAdapter  by lazy { CityAdapter() }
    private val cityViewModel: CityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCityBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)


        binding.apply {
            cityEditText.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    progressBar2.visibility = View.VISIBLE
                    cityViewModel.loadCities(s.toString(), 10).enqueue(object :Callback<CityResponseApi>{
                        override fun onResponse(
                            call: Call<CityResponseApi>,
                            response: Response<CityResponseApi>
                        ) {
                            if (response.isSuccessful) {
                                val data = response.body()
                                data?.let {
                                    progressBar2.visibility = View.GONE
                                    cityAdapter.differ.submitList(it)
                                    cityView.apply {
                                        layoutManager = LinearLayoutManager(
                                            this@AddCityActivity,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                        adapter = cityAdapter
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<CityResponseApi>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })
                }

            })
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}