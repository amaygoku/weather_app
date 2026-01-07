package com.example.firstapp.activity

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.firstapp.R
import com.example.firstapp.shared.PrefManager
import com.example.firstapp.shared.SharedData

class SettingActivity : AppCompatActivity() {
    private val prefManager by lazy { PrefManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val tempUnitSwitch: SwitchCompat = findViewById(R.id.temp_unit_switch)
        val backButton: ImageView = findViewById(R.id.back_button)
        val aboutButton: Button = findViewById(R.id.about_button)
        val exitButton: Button = findViewById(R.id.exit_button)

        // Thiết lập trạng thái ban đầu của Switch dựa trên dữ liệu đã lưu
        tempUnitSwitch.isChecked = SharedData.sharedUnit == "imperial"
        
        tempUnitSwitch.setOnCheckedChangeListener { _, isChecked ->
            val unit = if (isChecked) "imperial" else "metric"
            val unitName = if (isChecked) "Fahrenheit" else "Celsius"
            
            // 1. Cập nhật SharedData để dùng ngay
            SharedData.sharedUnit = unit
            
            // 2. Lưu vào SharedPreferences để dùng cho lần sau
            prefManager.saveUnit(unit)
            
            Toast.makeText(this, "Đã đổi đơn vị sang $unitName", Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            finish()
        }

        aboutButton.setOnClickListener {
            Toast.makeText(this, "Weather App v1.0", Toast.LENGTH_SHORT).show()
        }

        exitButton.setOnClickListener {
            finishAffinity() 
        }
    }
}
