package com.shid.mosquefinder.Ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.Data.Repository.AzkharRepository
import com.shid.mosquefinder.Ui.Main.ViewModel.AzkharViewModel
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