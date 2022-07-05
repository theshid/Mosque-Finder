package com.shid.mosquefinder.app.ui.base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.model.Api.GoogleApiInterface
import com.shid.mosquefinder.data.repository.MapRepository
import com.shid.mosquefinder.app.ui.main.view_models.MapViewModel

class MapViewModelFactory constructor(googleApiInterface: GoogleApiInterface, application: Application) : ViewModelProvider.Factory {
    val mApplication=application
    val mApiInterface = googleApiInterface

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(MapRepository(mApiInterface,mApplication),mApplication) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}