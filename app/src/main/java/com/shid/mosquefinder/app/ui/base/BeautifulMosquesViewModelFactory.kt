package com.shid.mosquefinder.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.repository.BeautifulMosquesRepository
import com.shid.mosquefinder.app.ui.main.view_models.BeautifulMosquesViewModel

class BeautifulMosquesViewModelFactory():ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeautifulMosquesViewModel::class.java)) {
            return BeautifulMosquesViewModel(BeautifulMosquesRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}