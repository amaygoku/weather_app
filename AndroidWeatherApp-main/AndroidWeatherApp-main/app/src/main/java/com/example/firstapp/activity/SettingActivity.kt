package com.example.firstapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firstapp.R
import com.example.firstapp.shared.SharedData

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // Initialize views
        val tempUnitSwitch: Switch = findViewById(R.id.temp_unit_switch)
        val backButton: ImageView = findViewById(R.id.back_button)
        val aboutButton: Button = findViewById(R.id.about_button)
        val exitButton: Button = findViewById(R.id.exit_button)

        // Handle Temperature Unit Switch
        tempUnitSwitch.isChecked = SharedData.unitString == "imperial"
        tempUnitSwitch.setOnCheckedChangeListener { _, isChecked ->
            val unit = if (isChecked) "Fahrenheit" else "Celsius"
            if(unit == "Fahrenheit"){
                SharedData.unitString = "imperial"
            } else {
                SharedData.unitString = "metric"
            }
            Toast.makeText(this, "Temperature unit set to $unit", Toast.LENGTH_SHORT).show()
        }
        backButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        // Handle About Button
        aboutButton.setOnClickListener {
            // Placeholder for "About" feature
            Toast.makeText(this, "About clicked. Feature not implemented yet.", Toast.LENGTH_SHORT).show()
        }

        // Handle Exit Button
        exitButton.setOnClickListener {
            // Exit the app
            finishAffinity() // Closes all activities in the task
        }
    }
}
