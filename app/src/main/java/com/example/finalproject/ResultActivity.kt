package com.example.finalproject

import android.Manifest
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.RequestOptions
import com.example.finalproject.tensorflow.ImageUtils
import com.example.finalproject.tensorflow.MLExecutionViewModel
import com.example.finalproject.tensorflow.ModelExecutionResult
import com.example.finalproject.tensorflow.StyleTransferModelExecutor
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.io.File
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.concurrent.Executors


// This is an arbitrary number we are using to keep tab of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts
private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

private const val TAG = "ResultActivity"

class ResultActivity :
    AppCompatActivity(){

    companion object {
        private const val ID = "com.example.finalproject"
        private val ID_BYTES = ID.toByteArray(Charset.forName("UTF-8"))
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        lateinit var inputPhotoPath : String
        lateinit var inputPhoto : Bitmap
        lateinit var inputStylePath : String
    }

    private var isRunningModel = false
    private var selectedStyle: String = ""

    private lateinit var viewModel: MLExecutionViewModel
    private lateinit var resultImageView: ImageView
    private lateinit var progressBar: ProgressBar

    private var lastSavedFile = ""
    private var useGPU = false
    private lateinit var styleTransferModelExecutor: StyleTransferModelExecutor
    private val inferenceThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val mainScope = MainScope()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        resultImageView = findViewById(R.id.result_imageview)
        progressBar = findViewById(R.id.indeterminateBar)

        viewModel = AndroidViewModelFactory(application).create(MLExecutionViewModel::class.java)

        viewModel.styledBitmap.observe(
                this,
                Observer { resultImage ->
                    if (resultImage != null) {
                        updateUIWithResults(resultImage)
                    }
                }
        )
        styleTransferModelExecutor = StyleTransferModelExecutor(baseContext, useGPU)

        lastSavedFile = inputPhotoPath
        selectedStyle = inputStylePath

        Log.d(TAG, "lastSavedFile")
        Log.d(TAG, inputPhotoPath)
        Log.d(TAG, "selectedStyle")
        Log.d(TAG, inputStylePath)
        //setImageView(originalImageView, inputPhoto)
        //setImageView(styleImageView, inputStylePath)
        Log.d(TAG, "startRunningModel()")
        startRunningModel()
    }

    override fun onPause() {
        super.onPause()
        if(resultImageView.visibility == View.VISIBLE){
            progressBar.visibility == View.INVISIBLE
        }
        else{
            progressBar.visibility == View.VISIBLE
        }
    }



    fun onQuestionMarkButtonClick(view: View) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog)
        dialog.setCanceledOnTouchOutside(true);
        dialog.window!!.decorView.setBackgroundResource(R.color.whitetransparent)
        dialog.show()
    }

    fun onDownloadButtonClick(view: View) {
        var test = inputPhoto.toString()
        var test2 = test.removeRange(0, 24)

        // Request camera permissions
        if (allPermissionsGranted()) {
            saveImageToGallery(resultImageView.drawToBitmap(), test2)
            Toast.makeText(this, "Image saved! ${test2}.jpg", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
            )
            Toast.makeText(this, "Click the generated image again!", Toast.LENGTH_SHORT).show()
        }
    }

    fun onShareButtonClick(view: View) {
        var test = inputPhoto.toString()
        var test2 = test.removeRange(0, 24)

        // Request camera permissions
        if (allPermissionsGranted()) {
            val uri = saveImageToGallery(resultImageView.drawToBitmap(), test2)
            Toast.makeText(this, "Image saved! ${test2}.jpg", Toast.LENGTH_SHORT).show()
            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/jpeg"

            share.putExtra(Intent.EXTRA_STREAM,uri)

            startActivity(Intent.createChooser(share, "Share Image"))
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
            )
            Toast.makeText(this, "Click the generated image again!", Toast.LENGTH_SHORT).show()
        }

    }

    fun onStartOverButtonClick(view: View) {
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun saveImageToGallery(bitmap: Bitmap, title: String):Uri{
        val savedImagePath = MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                title,
                "Style Transfer File #$title"
        )
        Log.d(TAG, savedImagePath)
        val file = File(savedImagePath)
        MediaScannerConnection.scanFile(baseContext, arrayOf(file.toString()),
                arrayOf(file.name), null)
        return Uri.parse(savedImagePath)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
            } else {
                Toast.makeText(
                        this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setImageView(imageView: ImageView, image: Bitmap) {
        Glide.with(baseContext)
            .load(image)
            .override(512, 512)
            .fitCenter()
            .into(imageView)
    }

    private fun setImageView(imageView: ImageView, imagePath: String) {
        Glide.with(baseContext)
            .asBitmap()
            .load(imagePath)
            .override(512, 512)
            .apply(RequestOptions().transform(CropTop()))
            .into(imageView)
    }

    private fun updateUIWithResults(modelExecutionResult: ModelExecutionResult) {
        resultImageView.visibility = View.VISIBLE
        setImageView(resultImageView, modelExecutionResult.styledImage)
        progressBar.visibility = View.INVISIBLE
    }

    private fun startRunningModel() {
        if (!isRunningModel && selectedStyle.isNotEmpty()) {
            //setImageView(styleImageView, selectedStyle)
            resultImageView.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            viewModel.onApplyStyle(
                    baseContext, lastSavedFile, selectedStyle, styleTransferModelExecutor,
                    inferenceThread
            )
            isRunningModel = true
        } else {
            Toast.makeText(this, "Previous Model still running", Toast.LENGTH_SHORT).show()
        }
    }

    class CropTop : BitmapTransformation() {
        override fun transform(
                pool: BitmapPool,
                toTransform: Bitmap,
                outWidth: Int,
                outHeight: Int
        ): Bitmap {
            return if (toTransform.width == outWidth && toTransform.height == outHeight) {
                toTransform
            } else ImageUtils.scaleBitmapAndKeepRatio(toTransform, outWidth, outHeight)
        }

        override fun equals(other: Any?): Boolean {
            return other is CropTop
        }

        override fun hashCode(): Int {
            return ID.hashCode()
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(ID_BYTES)
        }

        companion object {
            private const val ID = "org.tensorflow.lite.examples.styletransfer.CropTop"
            private val ID_BYTES = ID.toByteArray(Charset.forName("UTF-8"))
        }
    }
}
