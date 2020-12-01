package com.example.finalproject

import android.R.attr
import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap

private const val TAG = "PhotoActivity"

class ChoosePhotoActivity : AppCompatActivity() {
    val REQUEST_CODE = 100
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


    fun onChoosePhotoButtonClick(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            val imageView: ImageView = findViewById<View>(R.id.image_result) as ImageView
            imageView.setImageURI(data?.data) // handle chosen image
//            val uri: Uri? = data?.data
//            val file = File(uri?.path) //create path from uri
//            val split: List<String> = file.path.split(":") //split the path.
//            val filePath = split[1] //assign it to a string(your choice).
//            ResultActivity.Companion.inputPhotoPath = filePath
//              ResultActivity.Companion.inputPhoto = imageView.drawToBitmap()
            //ResultActivity.Companion.inputPhotoPath = data?.data.toString()
//            val file = File(data?.data?.path)
//            if (file.exists()) {
//
//
//            }
            val selectedImage: Uri = data?.data!!
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
            cursor!!.moveToFirst()

            val columnIndex = cursor!!.getColumnIndex(filePathColumn[0])
            val picturePath = cursor!!.getString(columnIndex)

            cursor!!.close()

            ResultActivity.Companion.inputPhotoPath = picturePath
            ResultActivity.Companion.inputPhoto = imageView.drawToBitmap()
        }
    }
}