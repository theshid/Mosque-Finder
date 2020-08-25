package com.shid.mosquefinder.Ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.Data.Repository.MapRepository
import com.shid.mosquefinder.Data.Repository.QuoteRepository
import com.shid.mosquefinder.Ui.Main.ViewModel.MapViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.QuotesViewModel

class QuotesViewModelFactory ():ViewModelProvider.Factory {


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuotesViewModel::class.java)) {
            return QuotesViewModel(QuoteRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}