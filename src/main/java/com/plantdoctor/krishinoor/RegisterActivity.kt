package com.plantdoctor.krishinoor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plantdoctor.krishinoor.databinding.ActivityRegisterBinding
import com.plantdoctor.krishinoor.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }

        binding.createAccountBtn.setOnClickListener {
            val name     = binding.nameInput.text.toString().trim()
            val phone    = binding.phoneInput.text.toString().trim()
            val location = binding.locationInput.text.toString().trim()
            val pass     = binding.passwordInput.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass.length < 4) {
                Toast.makeText(this, "Password must be at least 4 characters",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            getSharedPreferences("plant_doctor", MODE_PRIVATE).edit()
                .putString("user_name", name)
                .putString("user_phone", phone)
                .putString("user_location", location)
                .putString("reg_phone", phone)
                .putString("reg_pass", pass)
                .putBoolean("is_logged_in", true)
                .apply()

            Toast.makeText(this, "Welcome, $name! 🌿", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}
