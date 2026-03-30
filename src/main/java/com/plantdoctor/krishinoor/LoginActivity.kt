package com.plantdoctor.krishinoor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plantdoctor.krishinoor.databinding.ActivityLoginBinding
import com.plantdoctor.krishinoor.R
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySavedLanguage()

        // Skip login if already logged in
        val prefs = getSharedPreferences("plant_doctor", MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLanguageSpinner()
        setupButtons()
    }

    private fun setupLanguageSpinner() {
        val languages = resources.getStringArray(R.array.languages)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = adapter

        val prefs = getSharedPreferences("plant_doctor", MODE_PRIVATE)
        val savedLang = prefs.getString("language_code", "en")
        val codes = resources.getStringArray(R.array.language_codes)
        val savedIndex = codes.indexOf(savedLang).coerceAtLeast(0)
        binding.languageSpinner.setSelection(savedIndex)

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedCode = codes[pos]
                val currentCode = prefs.getString("language_code", "en")
                if (selectedCode != currentCode) {
                    prefs.edit().putString("language_code", selectedCode).apply()
                    setLocale(selectedCode)
                    recreate()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupButtons() {
        binding.loginBtn.setOnClickListener {
            val phone = binding.phoneInput.text.toString().trim()
            val pass  = binding.passwordInput.text.toString().trim()

            if (phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("plant_doctor", MODE_PRIVATE)
            val savedPhone = prefs.getString("reg_phone", null)
            val savedPass  = prefs.getString("reg_pass", null)

            if (savedPhone == null) {
                // First time — auto login for demo
                prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("user_phone", phone)
                    .putString("user_name", "Farmer")
                    .apply()
                startMain()
            } else if (phone == savedPhone && pass == savedPass) {
                prefs.edit().putBoolean("is_logged_in", true).apply()
                startMain()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun startMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun setLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun applySavedLanguage() {
        val prefs = getSharedPreferences("plant_doctor", MODE_PRIVATE)
        val langCode = prefs.getString("language_code", "en") ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
