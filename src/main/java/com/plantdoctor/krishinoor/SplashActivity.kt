package com.plantdoctor.krishinoor

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.plantdoctor.krishinoor.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("plant_doctor", MODE_PRIVATE)
            
            // To ensure you see the new Login Page during testing, 
            // I'm resetting the login state once.
            prefs.edit().putBoolean("is_logged_in", false).apply()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}
