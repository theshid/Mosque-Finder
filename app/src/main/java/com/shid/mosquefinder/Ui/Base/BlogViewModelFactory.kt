package com.shid.mosquefinder.Ui.Base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.Data.Repository.BlogRepository
import com.shid.mosquefinder.Ui.Main.ViewModel.BlogViewModel

class BlogViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlogViewModel::class.java)) {
            return BlogViewModel(BlogRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}