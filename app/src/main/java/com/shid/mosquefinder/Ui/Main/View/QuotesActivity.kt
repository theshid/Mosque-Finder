package com.shid.mosquefinder.Ui.Main.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.shid.mosquefinder.R

class QuotesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotes)
        Toast.makeText(this,"Coming soon", Toast.LENGTH_LONG).show()
    }
}