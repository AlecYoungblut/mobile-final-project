package com.example.finalproject

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import java.io.InputStream


private const val TAG = "StyAct"

class ChooseStyleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_style)
    }

    fun onPreviousButtonClick(view: View){
        val intent = Intent(this, ChoosePhotoActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    fun onNextButtonClick(view: View){
        val intent = Intent(this, ResultActivity::class.java)
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

    private fun getUriFromAssetName(name: String): String {
        return "file:///android_asset/styles/$name"
    }

    fun onStyleClick(view: View){
        when(view.id)
        {
            R.id.styleOne -> {
                ResultActivity.Companion.inputStylePath = getUriFromAssetName("style19.jpg")
                Log.d(TAG, getUriFromAssetName("style19"))
            }
        }
    }
}