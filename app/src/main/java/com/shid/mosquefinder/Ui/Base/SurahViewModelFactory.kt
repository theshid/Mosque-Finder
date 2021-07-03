package com.shid.mosquefinder.Ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.Ui.Main.ViewModel.SearchViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.SurahViewModel

class SurahViewModelFactory constructor( application: Application) : ViewModelProvider.Factory  {

    val mApplication = application

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurahViewModel::class.java)) {
            return SurahViewModel(mApplication) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}