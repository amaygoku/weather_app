package com.example.firstapp.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapp.R
import com.example.firstapp.adapter.CityAdapter
import com.example.firstapp.databinding.ActivityAddCityBinding
import com.example.firstapp.shared.PrefManager
import com.example.firstapp.viewmodel.CityViewModel

class AddCityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCityBinding
    private val cityAdapter by lazy { CityAdapter() }
    private val cityViewModel: CityViewModel by viewModels()
    private val prefManager by lazy { PrefManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCityBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.apply {
            cityView.layoutManager = LinearLayoutManager(this@AddCityActivity, LinearLayoutManager.VERTICAL, false)
            cityView.adapter = cityAdapter

            // Xử lý nút quay lại
            backBtn.setOnClickListener {
                finish()
            }

            showSearchHistory()

            cityEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val query = s.toString().trim()
                    if (query.length > 2) {
                        progressBar2.visibility = View.VISIBLE
                        cityViewModel.loadCities(query, 10)
                    } else if (query.isEmpty()) {
                        showSearchHistory()
                    }
                }
            })

            setupObservers()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showSearchHistory() {
        val history = prefManager.getHistory()
        if (history.isNotEmpty()) {
            cityAdapter.differ.submitList(history)
            binding.progressBar2.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        cityViewModel.cities.observe(this, Observer { data ->
            binding.progressBar2.visibility = View.GONE
            cityAdapter.differ.submitList(data)
        })

        cityViewModel.error.observe(this, Observer { error ->
            binding.progressBar2.visibility = View.GONE
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        })
    }
}
