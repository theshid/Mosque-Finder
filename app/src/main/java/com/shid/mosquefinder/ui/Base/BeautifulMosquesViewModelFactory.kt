package com.shid.mosquefinder.ui.Base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.repository.BeautifulMosquesRepository
import com.shid.mosquefinder.ui.Main.ViewModel.BeautifulMosquesViewModel

class BeautifulMosquesViewModelFactory():ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeautifulMosquesViewModel::class.java)) {
            return BeautifulMosquesViewModel(BeautifulMosquesRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}