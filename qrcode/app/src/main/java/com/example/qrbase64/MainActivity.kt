package com.example.qrbase64

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.qrbase64.databinding.ActivityMainBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.Base64

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectImage.setOnClickListener { selectImage() }
        binding.btnGenerateQr.setOnClickListener { generateQr() }
        binding.btnScanQr.setOnClickListener { scanQr() }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    private fun generateQr() {
        currentBitmap?.let { bitmap ->
            val base64 = Base64.getEncoder().encodeToString(bitmap.toByteArray())
            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(base64, BarcodeFormat.QR_CODE, 512, 512)
            val qrBitmap = BarcodeEncoder().createBitmap(bitMatrix)
            binding.ivQr.setImageBitmap(qrBitmap)
        }
    }

    private fun scanQr() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setCameraId(0)
            setBeepEnabled(false)
        }
        barcodeLauncher.launch(options)
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { base64 ->
            val imageBytes = Base64.getDecoder().decode(base64)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            binding.ivPreview.setImageBitmap(bitmap)
        }
    }

    private fun Bitmap.toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                currentBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                binding.ivPreview.setImageBitmap(currentBitmap)
            }
        }
    }
}
