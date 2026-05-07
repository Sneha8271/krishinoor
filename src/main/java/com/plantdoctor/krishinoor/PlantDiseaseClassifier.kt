package com.plantdoctor.krishinoor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.core.graphics.scale
import org.json.JSONArray
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PlantDiseaseClassifier(private val context: Context) {

    private var module: Module? = null
    private var classes: List<String> = emptyList()

    companion object {
        private const val TAG = "PlantClassifier"
        private val MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
        private val STD  = floatArrayOf(0.229f, 0.224f, 0.225f)

        @Throws(IOException::class)
        fun assetFilePath(context: Context, assetName: String): String {
            val file = File(context.filesDir, assetName)
            try {
                file.delete()
                context.assets.open(assetName).use { input ->
                    FileOutputStream(file).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                        output.flush()
                    }
                }
                Log.d(TAG, "Asset copied: $assetName (${file.length() / 1024}KB)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to copy asset: ${e.message}")
                throw IOException("Failed to copy asset '$assetName': ${e.message}")
            }
            return file.absolutePath
        }
    }

    init {
        try {
            Log.d(TAG, "Loading model...")
            val modelPath = assetFilePath(context, "model_mobile.ptl")
            module = LiteModuleLoader.load(modelPath)
            Log.d(TAG, "✅ Model loaded!")
            loadClasses()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Model init failed: ${e.message}", e)
        }
    }

    private fun loadClasses() {
        try {
            val json = context.assets.open("classes.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            classes = List(arr.length()) { arr.getString(it) }
            Log.d(TAG, "✅ Loaded ${classes.size} classes")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load classes: ${e.message}", e)
        }
    }

    data class Prediction(val label: String, val confidence: Float)

    fun predict(bitmap: Bitmap, rotationDegrees: Int = 0): Prediction {
        val mod = module ?: return Prediction("Model Initialization Failed", 0f)
        if (classes.isEmpty()) return Prediction("Classes Not Loaded", 0f)

        return try {
            // Ensure ARGB_8888 format
            val safe = if (bitmap.config != Bitmap.Config.ARGB_8888)
                bitmap.copy(Bitmap.Config.ARGB_8888, false) else bitmap

            // Fix rotation
            val rotated = if (rotationDegrees != 0) {
                val m = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                Bitmap.createBitmap(safe, 0, 0, safe.width, safe.height, m, true)
            } else safe

            // Resize smaller side to 256
            val minSide = minOf(rotated.width, rotated.height)
            val sf = 256f / minSide
            val scaled = rotated.scale((rotated.width * sf).toInt(), (rotated.height * sf).toInt())

            // Center crop to 224x224
            val x = (scaled.width - 224) / 2
            val y = (scaled.height - 224) / 2
            val cropped = Bitmap.createBitmap(scaled, x, y, 224, 224)

            // Ensure ARGB_8888 after crop
            val croppedSafe = if (cropped.config != Bitmap.Config.ARGB_8888)
                cropped.copy(Bitmap.Config.ARGB_8888, false) else cropped

            // Normalize
            val tensor = TensorImageUtils.bitmapToFloat32Tensor(croppedSafe, MEAN, STD)

            // Inference
            val scores = mod.forward(IValue.from(tensor)).toTensor().dataAsFloatArray
            Log.d(TAG, "Scores[0..4]: ${scores.take(5)}")
            Log.d(TAG, "Sum: ${scores.sum()}, Max: ${scores.max()}, Min: ${scores.min()}")

            // Find best index (works for both raw logits AND probabilities)
            var maxIdx = 0
            var maxVal = scores[0]
            for (i in scores.indices) {
                if (scores[i] > maxVal) { maxVal = scores[i]; maxIdx = i }
            }

            // Calculate confidence:
            // If sum ≈ 1.0 → already softmaxed → use directly
            // If sum >> 1.0 → raw logits → apply softmax
            val confidence: Float
            val sum = scores.sum()
            confidence = if (sum > 1.5f || sum < 0f) {
                // Raw logits — apply softmax
                Log.d(TAG, "Applying softmax (raw logits detected, sum=$sum)")
                val maxScore = scores.max()!!
                var expSum = 0.0
                val expScores = FloatArray(scores.size) {
                    kotlin.math.exp((scores[it] - maxScore).toDouble()).toFloat()
                        .also { v -> expSum += v }
                }
                (expScores[maxIdx] / expSum).toFloat() * 100f
            } else {
                // Already probabilities
                Log.d(TAG, "Using direct probability (sum=$sum)")
                maxVal * 100f
            }

            val label = if (maxIdx < classes.size) classes[maxIdx] else "Unknown"
            Log.d(TAG, "✅ $label @ ${"%.1f".format(confidence)}%")

            Prediction(label, confidence)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Inference error: ${e.message}", e)
            Prediction("Inference Failed", 0f)
        }
    }
}