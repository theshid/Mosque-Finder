package com.shid.mosquefinder.ui.main.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.shid.mosquefinder.R

class CreditsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)
        Toast.makeText(this,"Coming soon",Toast.LENGTH_LONG).show()
    }
}