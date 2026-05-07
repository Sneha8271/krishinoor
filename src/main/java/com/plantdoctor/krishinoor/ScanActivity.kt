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
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.location.*
import com.plantdoctor.krishinoor.databinding.ActivityScanBinding
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private var photoUri: Uri? = null
    private var photoFilePath: String? = null
    private var capturedBitmap: Bitmap? = null

    private lateinit var classifier: PlantDiseaseClassifier

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
                photoUri = it
                photoFilePath = null
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

        // Diagnostic - check what's in assets
        try {
            val files = assets.list("")
            android.util.Log.d("AssetCheck", "Assets found: ${files?.joinToString()}")
        } catch (e: Exception) {
            android.util.Log.e("AssetCheck", "Error listing assets: ${e.message}")
        }

// Initialize offline ML Engine
        classifier = PlantDiseaseClassifier(this)

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
            capturedBitmap?.let { analyzeImage(it, photoUri) }
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
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                getLastLocation()
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA)
            )
        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    currentLocation = loc
                    updateGpsDisplay(loc.latitude, loc.longitude)
                } else {
                    binding.gpsText.text = "📍 Awaiting GPS fix..."
                }
            }
        }
    }

    private fun latLonToGrid(lat: Double, lng: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lng >= 0) "E" else "W"
        return "$latDir${"%.3f".format(kotlin.math.abs(lat))}°" +
                " $lonDir${"%.3f".format(kotlin.math.abs(lng))}°"
    }

    private fun getRotation(filePath: String?, uri: Uri? = null): Int {
        return try {
            val exif = if (uri != null) {
                contentResolver.openInputStream(uri).use { s -> s?.let { ExifInterface(it) } }
            } else if (filePath != null) {
                ExifInterface(filePath)
            } else null

            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90  -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (_: Exception) { 0 }
    }

    private fun analyzeImage(bitmap: Bitmap, uri: Uri? = null) {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.analyzeBtn.isEnabled = false

        val timestamp = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val lat = currentLocation?.latitude ?: 0.0
        val lng = currentLocation?.longitude ?: 0.0

        saveBitmapToCache(bitmap)

        // Get rotation from file path or Uri
        val rotation = getRotation(photoFilePath, uri)

        // Offline ML Inference powered by PyTorch Mobile
        Thread {
            try {
                // Run inference
                val prediction = classifier.predict(bitmap, rotation)

                // Parse raw label e.g. "Tomato___Late_blight"
                val parts     = prediction.label.split("___")
                val plantName = parts[0].replace("_", " ")
                val disease   = if (parts.size > 1)
                    parts[1].replace("_", " ")
                else
                    prediction.label.replace("_", " ")

                // ✅ Get real severity + remedy from DiseaseDatabase
                val info = DiseaseDatabase.getInfo(prediction.label)

                val jsonStr = JSONObject().apply {
                    put("plant_name", plantName)
                    put("disease",    disease)
                    put("confidence", "%.1f".format(prediction.confidence))
                    put("severity",   info.severity)   // real severity
                    put("solution",   info.solution)   // real remedy steps
                    put("is_healthy", disease.contains("healthy", ignoreCase = true))
                }.toString()

                runOnUiThread {
                    hideLoading()
                    startActivity(
                        Intent(this@ScanActivity, ResultActivity::class.java).apply {
                            putExtra("json_response", jsonStr)
                            putExtra("timestamp",     timestamp)
                            putExtra("latitude",      lat)
                            putExtra("longitude",     lng)
                        }
                    )
                }
            } catch (e: Exception) {
                runOnUiThread {
                    hideLoading()
                    Toast.makeText(
                        this@ScanActivity,
                        "Inference failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun saveBitmapToCache(bmp: Bitmap) {
        try {
            val f  = File(cacheDir, "scan_preview.jpg")
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