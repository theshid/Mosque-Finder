package com.shid.mosquefinder.ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.repository.AzkharRepository
import com.shid.mosquefinder.ui.Main.ViewModel.AzkharViewModel
import java.lang.IllegalArgumentException

class AzkharViewModelFactory(application: Application) : ViewModelProvider.Factory {
    val mApplication = application
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AzkharViewModel::class.java)) {
            return AzkharViewModel(AzkharRepository(mApplication), mApplication) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}