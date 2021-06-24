package com.shid.mosquefinder.Ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.Data.Repository.AyahRepository
import com.shid.mosquefinder.Ui.Main.ViewModel.AyahViewModel


class AyahViewModelFactory constructor( application: Application,ayahRepository: AyahRepository) : ViewModelProvider.Factory  {

    val mApplication = application
    val repository = ayahRepository

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AyahViewModel::class.java)) {
            return AyahViewModel(mApplication,repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}