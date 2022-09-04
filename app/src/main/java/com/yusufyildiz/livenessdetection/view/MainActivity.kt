package com.yusufyildiz.livenessdetection.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.yusufyildiz.livenessdetection.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun restartClick(view : View)
    {
        Toast.makeText(this,"Clicked", Toast.LENGTH_SHORT).show()
        this.recreate()
    }

}