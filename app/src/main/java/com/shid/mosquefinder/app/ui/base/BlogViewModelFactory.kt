package com.shid.mosquefinder.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.repository.BlogRepositoryImpl
import com.shid.mosquefinder.app.ui.main.view_models.BlogViewModel

class BlogViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlogViewModel::class.java)) {
            return BlogViewModel(BlogRepositoryImpl()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}