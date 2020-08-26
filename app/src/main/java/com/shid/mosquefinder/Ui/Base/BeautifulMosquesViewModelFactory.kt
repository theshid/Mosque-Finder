package com.shid.mosquefinder.Ui.Base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.Data.Repository.BeautifulMosquesRepository
import com.shid.mosquefinder.Data.Repository.QuoteRepository
import com.shid.mosquefinder.Ui.Main.ViewModel.BeautifulMosquesViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.QuotesViewModel

class BeautifulMosquesViewModelFactory():ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeautifulMosquesViewModel::class.java)) {
            return BeautifulMosquesViewModel(BeautifulMosquesRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}