package com.shid.mosquefinder.Ui.Main.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.ViewModel.BlogViewModel

class BlogActivity : AppCompatActivity() {

    val mViewModel: BlogViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog)
    }
}