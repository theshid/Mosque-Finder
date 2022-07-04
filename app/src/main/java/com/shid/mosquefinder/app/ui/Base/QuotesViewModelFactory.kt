package com.shid.mosquefinder.app.ui.Base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.repository.QuoteRepository
import com.shid.mosquefinder.app.ui.main.view_models.QuotesViewModel

class QuotesViewModelFactory ():ViewModelProvider.Factory {


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuotesViewModel::class.java)) {
            return QuotesViewModel(QuoteRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}