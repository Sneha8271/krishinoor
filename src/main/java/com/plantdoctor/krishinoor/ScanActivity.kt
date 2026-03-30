package com.plantdoctor.krishinoor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.location.*
import com.plantdoctor.krishinoor.databinding.ActivityScanBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private var photoUri: Uri? = null
    private var photoFilePath: String? = null
    private var capturedBitmap: Bitmap? = null

    // ⚠️ CHANGE THIS URL:
    // Emulator  → "http://10.0.2.2:8000/predict"
    // Real phone (same WiFi) → "http://192.168.X.X:8000/predict"
    // Deployed  → "https://your-app.railway.app/predict"
    private val API_URL = "http://10.6.156.134:8000/predict"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoFilePath?.let { path ->
                    capturedBitmap = BitmapFactory.decodeFile(path)
                    showPreview()
                    readExifGps(path)
                }
            }
        }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val stream = contentResolver.openInputStream(it)
                capturedBitmap = BitmapFactory.decodeStream(stream)
                showPreview()
                // Read GPS from UAV/drone photo EXIF
                contentResolver.openInputStream(it)?.let { s ->
                    val exif = ExifInterface(s)
                    val latLon = FloatArray(2)
                    if (exif.getLatLong(latLon)) {
                        updateGpsDisplay(latLon[0].toDouble(), latLon[1].toDouble(), true)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestPermissionsAndLocation()

        binding.backBtn.setOnClickListener { finish() }

        binding.cameraBtn.setOnClickListener {
            val f = createImageFile()
            photoFilePath = f.absolutePath
            photoUri = FileProvider.getUriForFile(
                this, "${packageName}.fileprovider", f)
            takePicture.launch(photoUri)
        }

        binding.galleryBtn.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.analyzeBtn.setOnClickListener {
            capturedBitmap?.let { analyzeImage(it) }
        }
    }

    private fun showPreview() {
        binding.previewImage.setImageBitmap(capturedBitmap)
        binding.previewImage.visibility = View.VISIBLE
        binding.placeholderLayout.visibility = View.GONE
        binding.analyzeBtn.isEnabled = true
        binding.analyzeBtn.alpha = 1.0f
    }

    private fun createImageFile(): File {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PLANT_${ts}_", ".jpg", dir)
    }

    private fun readExifGps(filePath: String) {
        try {
            val exif = ExifInterface(filePath)
            val latLon = FloatArray(2)
            if (exif.getLatLong(latLon)) {
                updateGpsDisplay(latLon[0].toDouble(), latLon[1].toDouble(), true)
            }
        } catch (_: Exception) {}
    }

    private fun updateGpsDisplay(lat: Double, lng: Double, fromExif: Boolean = false) {
        val src = if (fromExif) "📡 UAV GPS" else "📍 GPS"
        binding.gpsText.text =
            "$src: ${"%.5f".format(lat)}°N, ${"%.5f".format(lng)}°E"
        binding.gridText.text = "🗺️ Grid: ${latLonToGrid(lat, lng)}"
        currentLocation = android.location.Location("src").apply {
            latitude = lat; longitude = lng
        }
    }

    private fun requestPermissionsAndLocation() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION), 101
        )
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                currentLocation = loc
                loc?.let { updateGpsDisplay(it.latitude, it.longitude) }
            }
        }
    }

    private fun latLonToGrid(lat: Double, lng: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lng >= 0) "E" else "W"
        return "$latDir${"%.3f".format(Math.abs(lat))}°" +
                " $lonDir${"%.3f".format(Math.abs(lng))}°"
    }

    private fun analyzeImage(bitmap: Bitmap) {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.analyzeBtn.isEnabled = false

        val timestamp = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val lat = currentLocation?.latitude ?: 0.0
        val lng = currentLocation?.longitude ?: 0.0

        val out = ByteArrayOutputStream()
        scaleBitmap(bitmap, 800).compress(Bitmap.CompressFormat.JPEG, 80, out)
        val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)

        saveBitmapToCache(bitmap)

        val json = JSONObject().apply {
            put("image", b64)
            put("latitude", lat)
            put("longitude", lng)
            put("timestamp", timestamp)
        }

        val request = Request.Builder().url(API_URL)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    hideLoading()
                    Toast.makeText(this@ScanActivity,
                        "❌ Cannot reach backend!\n" +
                                "Make sure uvicorn is running.\n${e.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                runOnUiThread {
                    hideLoading()
                    if (response.isSuccessful && body != null) {
                        startActivity(
                            Intent(this@ScanActivity, ResultActivity::class.java).apply {
                                putExtra("json_response", body)
                                putExtra("timestamp", timestamp)
                                putExtra("latitude", lat)
                                putExtra("longitude", lng)
                            }
                        )
                    } else {
                        Toast.makeText(this@ScanActivity,
                            "Server error ${response.code}",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun scaleBitmap(bmp: Bitmap, max: Int): Bitmap {
        val r = minOf(max.toFloat() / bmp.width, max.toFloat() / bmp.height)
        if (r >= 1f) return bmp
        return Bitmap.createScaledBitmap(
            bmp, (bmp.width * r).toInt(), (bmp.height * r).toInt(), true)
    }

    private fun saveBitmapToCache(bmp: Bitmap) {
        try {
            val f = File(cacheDir, "scan_preview.jpg")
            val os = f.outputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, os)
            os.close()
        } catch (_: Exception) {}
    }

    private fun hideLoading() {
        binding.loadingLayout.visibility = View.GONE
        binding.analyzeBtn.isEnabled = true
    }
}
