package com.plantdoctor.krishinoor

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.plantdoctor.krishinoor.databinding.ActivityMainBinding
import com.plantdoctor.krishinoor.R
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupStats()
        setupRecentScan()
        setupButtons()
    }

    private fun setupHeader() {
        val prefs = getSharedPreferences("plant_doctor", MODE_PRIVATE)
        val name = prefs.getString("user_name", "Farmer") ?: "Farmer"

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> getString(R.string.good_morning)
            hour < 17 -> getString(R.string.good_afternoon)
            else -> getString(R.string.good_evening)
        }
        binding.greetingText.text = greeting
        binding.dateText.text = SimpleDateFormat(
            "EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    private fun setupStats() {
        val prefs = getSharedPreferences("plant_doctor", MODE_PRIVATE)
        val total    = prefs.getInt("total_scans", 0)
        val diseased = prefs.getInt("diseased_count", 0)
        val healthy  = (total - diseased).coerceAtLeast(0)

        binding.totalScansValue.text = total.toString()
        binding.diseasedValue.text   = diseased.toString()
        binding.healthyValue.text    = healthy.toString()
    }

    private fun setupRecentScan() {
        val prefs = getSharedPreferences("plant_doctor", MODE_PRIVATE)
        val lastPlant = prefs.getString("last_plant", null)

        if (lastPlant != null) {
            binding.recentCard.visibility = View.VISIBLE
            binding.noScanText.visibility = View.GONE
            binding.recentPlantName.text  = lastPlant
            binding.recentDisease.text    = prefs.getString("last_disease", "—")
            binding.recentTime.text       = prefs.getString("last_time", "—")
            binding.recentGps.text        = prefs.getString("last_gps", "—")
        } else {
            binding.recentCard.visibility = View.GONE
            binding.noScanText.visibility = View.VISIBLE
        }
    }

    private fun setupButtons() {
        binding.scanBtnQuick.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        binding.historyBtnQuick.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.scanFab.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        
        binding.logoutBtn.setOnClickListener {
            getSharedPreferences("plant_doctor", MODE_PRIVATE)
                .edit().putBoolean("is_logged_in", false).apply()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        setupStats()
        setupRecentScan()
    }
}
