package com.shid.mosquefinder.app.ui.main.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreditsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)
        showToast("Coming Soon")
    }
}