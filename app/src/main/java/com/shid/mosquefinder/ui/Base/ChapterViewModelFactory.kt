package com.shid.mosquefinder.ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.repository.ChapterRepository
import com.shid.mosquefinder.ui.Main.ViewModel.ChapterViewModel

class ChapterViewModelFactory(application: Application,chapterRepository: ChapterRepository)
    :ViewModelProvider.Factory {

    val mApplication = application
    val repository = chapterRepository

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChapterViewModel::class.java)) {
            return ChapterViewModel(mApplication,repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}