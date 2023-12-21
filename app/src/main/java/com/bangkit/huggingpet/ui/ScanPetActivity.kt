package com.bangkit.huggingpet.ui

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bangkit.huggingpet.R
import com.bangkit.huggingpet.databinding.ActivityScanPetBinding
import com.bangkit.huggingpet.viewmodel.UploadViewModel
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class ScanPetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanPetBinding
    private var getFile: File? = null

    private val uploadViewModel by viewModels<UploadViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanPetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ifClicked()

        uploadViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        uploadViewModel.prediction.observe(this) { result ->
            result?.let {
                Toast.makeText(this, "Prediction: $result", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ifClicked() {
        binding.btnScan.setOnClickListener {
            startTakePhoto()
        }

        binding.btnGallery.setOnClickListener {
            startGallery()
        }

        binding.buttonScan.setOnClickListener {
            getDisease()
        }
    }

    private fun reduceFileImage(file: File): File {
        var compressedFile: File = file

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                var compressedFileSize = file.length()

                while (compressedFileSize > 1 * 1024 * 1024) {
                    compressedFile = withContext(Dispatchers.Default) {
                        Compressor.compress(applicationContext, file)
                    }
                    compressedFileSize = compressedFile.length()
                }

                Log.d("ReduceImage", "Original File size: ${file.length()}")
                Log.d("ReduceImage", "Compressed File size: ${compressedFile.length()}")
            }
        }

        return compressedFile
    }

    private fun getDisease() {
        if (getFile == null) {
            Toast.makeText(this, "Please insert a picture", Toast.LENGTH_SHORT).show()
        } else {
            val file = reduceFileImage(getFile as File)
            val requestFile = file.asRequestBody("image/*".toMediaType())
            val imageMultipart: MultipartBody.Part =
                MultipartBody.Part.createFormData("data", file.name, requestFile)

            Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show()

            uploadViewModel.upload(imageMultipart)
        }
    }

    private var anyPhoto = false
    private lateinit var currentPhotoPath: String
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile
            val result = BitmapFactory.decodeFile(myFile.path)
            anyPhoto = true
            binding.imageScanResult.setImageBitmap(result)
        }
    }

    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@ScanPetActivity,
                getString(R.string.package_name),
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@ScanPetActivity)
            getFile = myFile
            binding.imageScanResult.setImageURI(selectedImg)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
        }
    companion object {
        const val FILENAME_FORMAT = "MMddyyyy"
    }
}

