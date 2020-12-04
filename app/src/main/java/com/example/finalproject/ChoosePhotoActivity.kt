package com.example.finalproject

import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import java.io.File


private const val TAG = "PhotAct"

class ChoosePhotoActivity : AppCompatActivity() {
    val REQUEST_IMAGE_PICKER = 100
    val REQUEST_IMAGE_CAPTURE = 200
    lateinit var currentPhotoPath: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_photo)
    }

    fun onPreviousButtonClick(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    fun onNextButtonClick(view: View) {
        val intent = Intent(this, ChooseStyleActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
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

    fun onTakePhotoButtonClick(view: View) {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(
            intent, REQUEST_IMAGE_CAPTURE, ActivityOptions.makeSceneTransitionAnimation(
                this
            ).toBundle()
        )
    }

    fun onChoosePhotoButtonClick(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICKER)
    }

    private fun getLastTakenPicture(): String {
        val directory = baseContext.filesDir // externalMediaDirs.first()
        var files =
            directory.listFiles()?.filter { file -> file.absolutePath.endsWith(".jpg") }?.sorted()
        if (files == null || files.isEmpty()) {
            Log.d(TAG, "there is no previous saved file")
            return ""
        }

        val file = files.last()
        Log.d(TAG, "lastsavedfile: " + file.absolutePath)
        return file.absolutePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            val imageView: ImageView = findViewById<View>(R.id.image_result) as ImageView
            //set imageView
            imageView.setImageURI(Uri.fromFile(File(getLastTakenPicture())));
            //imageView.setImageURI(Uri.parse(data?.getStringExtra("photo_uri_string")))
            ResultActivity.Companion.inputPhotoPath = getLastTakenPicture()
            ResultActivity.Companion.inputPhoto = imageView.drawToBitmap()
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_PICKER) {
            val imageView: ImageView = findViewById<View>(R.id.image_result) as ImageView
            imageView.setImageURI(data?.data) // handle chosen image
            val selectedImage: Uri = data?.data!!
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor!!.getColumnIndex(filePathColumn[0])
            val picturePath = cursor!!.getString(columnIndex)
            val uriPathHelper = URIPathHelper()
            val filePath = uriPathHelper.getPath(this, data?.data!!)
            // val directory = baseContext.filesDir
            Log.d(TAG, "App Directory")
            Log.d(TAG, baseContext.filesDir.toString())
            Log.d(TAG, "Picture Path From File")
            data?.data!!.path?.let { Log.d(TAG, it) }
            Log.d(TAG, "Picture Path From Code")
            Log.d(TAG, picturePath)
            Log.d(TAG, "Picture Path From Helper Method")
            filePath?.let { Log.d(TAG, it) }
            cursor!!.close()
            //ResultActivity.Companion.inputPhotoFile = File(Uri.decode(data?.data!!.toString()));
            ResultActivity.Companion.inputPhotoPath = picturePath
            ResultActivity.Companion.inputPhoto = imageView.drawToBitmap()
        }
    }
}