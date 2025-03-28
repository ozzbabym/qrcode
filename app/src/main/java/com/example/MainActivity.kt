import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.ByteArrayOutputStream
import java.util.Base64

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var qrImageView: ImageView
    private var selectedImage: Bitmap? = null
    private var base64String: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        qrImageView = findViewById(R.id.qrImageView)

        // 1. Выбор изображения
        findViewById<Button>(R.id.btnSelectImage).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }

        // 2. Генерация QR-кода из Base64
        findViewById<Button>(R.id.btnGenerateQR).setOnClickListener {
            if (selectedImage != null) {
                base64String = bitmapToBase64(selectedImage!!)
                generateQR(base64String)
            } else {
                Toast.makeText(this, "Сначала выберите изображение", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Сканирование QR и показ изображения
        findViewById<Button>(R.id.btnScanQR).setOnClickListener {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Наведите на QR-код")
                setCameraId(0)
                setBeepEnabled(false)
            }
            barcodeLauncher.launch(options)
        }
    }

    // Конвертация изображения в Base64
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }

    // Генерация QR-кода
    private fun generateQR(text: String) {
        try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 500, 500)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            qrImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Сканирование QR
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            base64String = result.contents
            val bitmap = base64ToBitmap(base64String)
            imageView.setImageBitmap(bitmap)
        }
    }

    // Конвертация Base64 в Bitmap
    private fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.getDecoder().decode(base64)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    // Получение выбранного изображения
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            selectedImage = bitmap
            imageView.setImageBitmap(bitmap)
        }
    }
}
