package com.shid.mosquefinder.app.ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.repository.AzkharRepositoryImpl
import com.shid.mosquefinder.app.ui.main.view_models.AzkharViewModel
import java.lang.IllegalArgumentException

class AzkharViewModelFactory(application: Application) : ViewModelProvider.Factory {
    val mApplication = application
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AzkharViewModel::class.java)) {
            return AzkharViewModel(AzkharRepositoryImpl(mApplication), mApplication) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}