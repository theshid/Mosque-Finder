package com.shid.mosquefinder.ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.ui.main.view_models.SplashViewModel

class SplashViewModelFactory constructor(application: Application): ViewModelProvider.Factory {
val mApplication=application
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            return SplashViewModel(
                mApplication
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}