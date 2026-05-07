package com.plantdoctor.krishinoor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.plantdoctor.krishinoor.databinding.ActivityResultBinding
import com.plantdoctor.krishinoor.R
import org.json.JSONObject
import java.io.File

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jsonStr   = intent.getStringExtra("json_response") ?: return
        val timestamp = intent.getStringExtra("timestamp") ?: ""
        val lat       = intent.getDoubleExtra("latitude", 0.0)
        val lng       = intent.getDoubleExtra("longitude", 0.0)

        displayResults(jsonStr, timestamp, lat, lng)
        loadPreview()

        binding.backBtn.setOnClickListener { finish() }
        binding.newScanBtn.setOnClickListener { finish() }
    }

    private fun displayResults(jsonStr: String, timestamp: String,
                               lat: Double, lng: Double) {
        val json       = JSONObject(jsonStr)
        val plantName  = json.optString("plant_name", "Unknown")
        val disease    = json.optString("disease", "Unknown")
        val confidence = json.optString("confidence", "0").toFloatOrNull() ?: 0f
        val severity   = json.optString("severity", "N/A")
        val solution   = json.optString("solution", getSolution(disease))
        val isHealthy  = json.optBoolean("is_healthy", disease.contains("healthy", ignoreCase = true))
        val gridCoord  = json.optString("grid_coordinate", latLonToGrid(lat, lng))

        // Confidence Threshold Check
        val isReliable = confidence > 60.0f

        // Plant name & confidence
        if (isReliable) {
            binding.plantNameText.text  = plantName
            binding.confidenceText.text = "Confidence: ${"%.1f".format(confidence)}%"
        } else {
            binding.plantNameText.text  = "Inconclusive ($plantName?)"
            binding.confidenceText.text = "⚠️ Low Confidence: ${"%.1f".format(confidence)}% (Try a clearer photo)"
        }
        binding.confidenceBar.progress = confidence.toInt()

        // Disease status & Severity
        if (isHealthy) {
            binding.diseaseText.text = "✅ Healthy Plant"
            binding.diseaseText.setTextColor(getColor(R.color.healthy_green))
            binding.statusBadge.text = "HEALTHY"
            binding.statusBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.healthy_green))
            binding.statusBadge.setTextColor(getColor(R.color.white))
            binding.statusIcon.text = "✅"
        } else {
            binding.diseaseText.text = "⚠️ $disease (Severity: $severity)"
            binding.diseaseText.setTextColor(getColor(R.color.disease_red))
            binding.statusBadge.text = "DISEASED"
            binding.statusBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.disease_red))
            binding.statusBadge.setTextColor(getColor(R.color.white))
            binding.statusIcon.text = "⚠️"
        }

        // Location
        binding.gpsText.text  = if (lat != 0.0)
            "📍 Lat: ${"%.5f".format(lat)}°   Lon: ${"%.5f".format(lng)}°"
        else "📍 GPS not available"
        binding.gridText.text = "🗺️ Grid: $gridCoord"
        binding.timeText.text = "🕐 $timestamp"

        // Solution
        binding.solutionText.text = solution

        saveToHistory(plantName, disease, timestamp, lat, lng, gridCoord)
        sendNotification(plantName, disease, isHealthy)
    }

    private fun latLonToGrid(lat: Double, lng: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lng >= 0) "E" else "W"
        return "$latDir${"%.3f".format(Math.abs(lat))}°" +
                " $lonDir${"%.3f".format(Math.abs(lng))}°"
    }

    private fun getSolution(disease: String): String = when {
        disease.contains("blight", true)  ->
            "• Remove infected leaves immediately\n" +
                    "• Apply copper-based fungicide every 7 days\n" +
                    "• Avoid overhead watering\n" +
                    "• Improve air circulation\n" +
                    "• Destroy infected plant material"
        disease.contains("rust", true)    ->
            "• Apply sulfur-based fungicide\n" +
                    "• Remove and destroy infected material\n" +
                    "• Avoid wetting foliage\n" +
                    "• Plant resistant varieties next season\n" +
                    "• Spray neem oil as preventive measure"
        disease.contains("mildew", true)  ->
            "• Apply neem oil or potassium bicarbonate\n" +
                    "• Increase air circulation\n" +
                    "• Reduce humidity near plants\n" +
                    "• Avoid overhead irrigation\n" +
                    "• Prune dense foliage"
        disease.contains("spot", true)    ->
            "• Apply copper fungicide weekly\n" +
                    "• Remove infected leaves carefully\n" +
                    "• Avoid splashing water on leaves\n" +
                    "• Maintain proper plant spacing\n" +
                    "• Mulch to prevent soil splash"
        disease.contains("mosaic", true)  ->
            "• No chemical cure — act fast!\n" +
                    "• Remove infected plants immediately\n" +
                    "• Control aphid/insect population\n" +
                    "• Use virus-free certified seeds\n" +
                    "• Disinfect tools between plants"
        disease.contains("healthy", true) ->
            "✅ Your plant is healthy!\n\n" +
                    "• Maintain regular watering schedule\n" +
                    "• Balanced NPK fertilization\n" +
                    "• Monitor weekly for early signs\n" +
                    "• Ensure proper field drainage\n" +
                    "• Keep field clean of weeds"
        else ->
            "• Consult local agricultural officer\n" +
                    "• Remove severely infected parts\n" +
                    "• Apply broad-spectrum fungicide\n" +
                    "• Monitor surrounding plants\n" +
                    "• Ensure proper field drainage"
    }

    private fun saveToHistory(plant: String, disease: String, time: String,
                              lat: Double, lng: Double, grid: String) {
        val prefs  = getSharedPreferences("plant_doctor", MODE_PRIVATE)
        val editor = prefs.edit()
        val total  = prefs.getInt("total_scans", 0) + 1
        editor.putInt("total_scans", total)
        if (!disease.contains("healthy", true)) {
            editor.putInt("diseased_count",
                prefs.getInt("diseased_count", 0) + 1)
        }
        editor.putString("last_plant",   plant)
        editor.putString("last_disease", disease)
        editor.putString("last_time",    time)
        editor.putString("last_gps",
            "📍 ${"%.5f".format(lat)}, ${"%.5f".format(lng)}")
        editor.putString("last_grid",    "🗺️ $grid")
        editor.apply()
    }

    private fun loadPreview() {
        val f = File(cacheDir, "scan_preview.jpg")
        if (f.exists())
            binding.previewImage.setImageBitmap(BitmapFactory.decodeFile(f.absolutePath))
    }

    private fun sendNotification(plantName: String, disease: String,
                                 isHealthy: Boolean) {
        val channelId = "plant_doctor_alerts"
        val manager   = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(channelId, "Plant Alerts",
                NotificationManager.IMPORTANCE_HIGH)
        )
        val title = if (isHealthy) "✅ Plant is Healthy!" else "⚠️ Disease Detected!"
        val msg   = if (isHealthy)
            "$plantName looks healthy — no action needed."
        else "$plantName: $disease found. Open app for treatment."

        manager.notify(System.currentTimeMillis().toInt(),
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(msg)
                .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
        )
    }
}
