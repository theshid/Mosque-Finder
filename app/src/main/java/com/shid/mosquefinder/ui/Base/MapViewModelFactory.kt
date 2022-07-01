package com.shid.mosquefinder.ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.model.Api.ApiInterface
import com.shid.mosquefinder.data.repository.MapRepository
import com.shid.mosquefinder.ui.Main.ViewModel.MapViewModel

class MapViewModelFactory constructor(apiInterface: ApiInterface,application: Application) : ViewModelProvider.Factory {
    val mApplication=application
    val mApiInterface = apiInterface

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(MapRepository(mApiInterface,mApplication),mApplication) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}