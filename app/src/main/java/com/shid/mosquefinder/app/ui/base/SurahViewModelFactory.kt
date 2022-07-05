package com.shid.mosquefinder.app.ui.base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.app.ui.main.view_models.SurahViewModel

class SurahViewModelFactory constructor( application: Application) : ViewModelProvider.Factory  {

    val mApplication = application

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurahViewModel::class.java)) {
            return SurahViewModel(mApplication) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}