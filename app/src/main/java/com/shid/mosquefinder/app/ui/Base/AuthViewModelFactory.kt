package com.shid.mosquefinder.app.ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.app.ui.main.view_models.AuthViewModel

class AuthViewModelFactory constructor(application: Application) : ViewModelProvider.Factory {

    private val mApplication = application

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(

                mApplication

            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}