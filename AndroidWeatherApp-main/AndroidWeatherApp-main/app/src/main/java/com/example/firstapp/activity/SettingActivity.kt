package com.example.firstapp.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.firstapp.R
import com.example.firstapp.shared.PrefManager
import com.example.firstapp.shared.SharedData
import java.util.Locale

class SettingActivity : AppCompatActivity() {
    private val prefManager by lazy { PrefManager(this) }

    /**
     * Khởi tạo Activity cài đặt
     * Thiết lập các Switch cho nhiệt độ và ngôn ngữ, xử lý sự kiện nút
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved language before setting content view
        val savedLanguage = prefManager.getLanguage()
        setLocale(savedLanguage)
        
        setContentView(R.layout.activity_setting)

        val tempUnitSwitch: SwitchCompat = findViewById(R.id.temp_unit_switch)
        val languageSwitch: SwitchCompat = findViewById(R.id.language_switch)
        val languageIndicator: TextView = findViewById(R.id.language_indicator)
        val backButton: ImageView = findViewById(R.id.back_button)
        val aboutButton: Button = findViewById(R.id.about_button)
        val exitButton: Button = findViewById(R.id.exit_button)

        // Thiết lập trạng thái ban đầu của Temperature Switch
        tempUnitSwitch.isChecked = SharedData.sharedUnit == "imperial"
        
        // Thiết lập trạng thái ban đầu của Language Switch
        val currentLang = prefManager.getLanguage()
        languageSwitch.isChecked = currentLang == "en"
        updateLanguageIndicator(languageIndicator, currentLang)
        
        // Temperature Unit Switch Handler
        tempUnitSwitch.setOnCheckedChangeListener { _, isChecked ->
            val unit = if (isChecked) "imperial" else "metric"
            val unitName = if (isChecked) getString(R.string.fahrenheit) else getString(R.string.celsius)
            
            SharedData.sharedUnit = unit
            prefManager.saveUnit(unit)
            
            Toast.makeText(this, getString(R.string.changed_unit, unitName), Toast.LENGTH_SHORT).show()
        }
        
        // Language Switch Handler
        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            val languageCode = if (isChecked) "en" else "vi"
            
            prefManager.saveLanguage(languageCode)
            updateLanguageIndicator(languageIndicator, languageCode)
            setLocale(languageCode)
            
            Toast.makeText(this, getString(R.string.language_changed), Toast.LENGTH_LONG).show()
            
            // Recreate activity để áp dụng ngôn ngữ mới
            recreate()
        }

        backButton.setOnClickListener {
            finish()
        }

        aboutButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.about_message), Toast.LENGTH_SHORT).show()
        }

        exitButton.setOnClickListener {
            finishAffinity() 
        }
    }
    
    /**
     * Thay đổi ngôn ngữ hiển thị của ứng dụng
     * @param languageCode Mã ngôn ngữ ("vi" hoặc "en")
     */
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    
    /**
     * Cập nhật text hiển thị ngôn ngữ hiện tại
     * @param indicator TextView hiển thị ngôn ngữ
     * @param languageCode Mã ngôn ngữ hiện tại
     */
    private fun updateLanguageIndicator(indicator: TextView, languageCode: String) {
        indicator.text = if (languageCode == "en") {
            "🇬🇧 English"
        } else {
            "🇻🇳 Tiếng Việt"
        }
    }
}
