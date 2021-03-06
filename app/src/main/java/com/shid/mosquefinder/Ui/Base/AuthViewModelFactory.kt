package com.shid.mosquefinder.Ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.Data.Model.Api.ApiInterface
import com.shid.mosquefinder.Data.Repository.MapRepository
import com.shid.mosquefinder.Ui.Main.ViewModel.AuthViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.MapViewModel

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