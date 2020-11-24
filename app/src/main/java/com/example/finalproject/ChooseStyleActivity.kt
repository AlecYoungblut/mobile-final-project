package com.example.finalproject

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity


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
        val intent = Intent(this, MainActivity::class.java)
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

    fun onStyleClick(view: View){

        when(view.id)
        {
            R.id.styleOne -> {
//                val btn = findViewById<View>(R.id.styleOne) as ImageButton
//                val drawable = btn.drawable
//                if (drawable.constantState == resources.getDrawable(R.drawable.style19).constantState) {
//                    ResultActivity.Companion.style.setImageResource(drawable)
//                }
            }
        }
    }
}