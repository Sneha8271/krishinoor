package com.plantdoctor.krishinoor

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.plantdoctor.krishinoor.databinding.ActivityHistoryBinding
import com.plantdoctor.krishinoor.R

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }

        val prefs     = getSharedPreferences("plant_doctor", MODE_PRIVATE)
        val lastPlant = prefs.getString("last_plant", null)

        if (lastPlant != null) {
            val disease   = prefs.getString("last_disease", "—") ?: "—"
            val isHealthy = disease.contains("healthy", ignoreCase = true)

            binding.statusIcon.text = if (isHealthy) "✅" else "⚠️"
            binding.plantName.text  = lastPlant
            binding.diseaseText.text = disease
            binding.diseaseText.setTextColor(
                if (isHealthy) getColor(R.color.healthy_green)
                else getColor(R.color.disease_red)
            )
            binding.gpsText.text  = prefs.getString("last_gps", "—")
            binding.gridText.text = prefs.getString("last_grid", "—")
            binding.timeText.text = prefs.getString("last_time", "—")

            val total    = prefs.getInt("total_scans", 0)
            val diseased = prefs.getInt("diseased_count", 0)
            val healthy  = total - diseased

            binding.statTotal.text    = total.toString()
            binding.statDiseased.text = diseased.toString()
            binding.statHealthy.text  = healthy.toString()

            binding.emptyState.visibility    = View.GONE
            binding.historyContent.visibility = View.VISIBLE
        } else {
            binding.emptyState.visibility    = View.VISIBLE
            binding.historyContent.visibility = View.GONE
        }
    }
}
