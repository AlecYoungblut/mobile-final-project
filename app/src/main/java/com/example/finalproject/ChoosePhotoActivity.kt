package com.example.finalproject

import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import java.io.File


private const val TAG = "PhotAct"

class ChoosePhotoActivity : AppCompatActivity() {
    val REQUEST_IMAGE_PICKER = 100
    val REQUEST_IMAGE_CAPTURE = 200
    val CANCEL_IMAGE_CAPTURE = 300
    var isImageSelected = false;
    lateinit var imageView: ImageView
    lateinit var currentPhotoPath: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_photo)
        imageView = findViewById<View>(R.id.image_result) as ImageView
    }

    fun onPreviousButtonClick(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    fun onNextButtonClick(view: View) {
        if(isImageSelected){
            val intent = Intent(this, ChooseStyleActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
        else{
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_LONG).show()
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
        val directory = baseContext.filesDir
        var files =
            directory.listFiles()?.filter { file -> file.absolutePath.endsWith(".jpg") }?.sorted()
        if (files == null || files.isEmpty()) {
            return ""
        }

        val file = files.last()
        return file.absolutePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            isImageSelected = true;

            imageView.setImageURI(Uri.fromFile(File(getLastTakenPicture())));

            ResultActivity.inputPhotoPath = getLastTakenPicture()
            ResultActivity.inputPhoto = imageView.drawToBitmap()
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_PICKER) {
            isImageSelected = true;

            imageView.setImageURI(data?.data) // handle chosen image

            val selectedImage: Uri = data?.data!!
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            ResultActivity.inputPhotoPath = picturePath
            ResultActivity.inputPhoto = imageView.drawToBitmap()
        }
    }
}