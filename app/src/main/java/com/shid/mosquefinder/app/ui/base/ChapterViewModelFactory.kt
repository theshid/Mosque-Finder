package com.shid.mosquefinder.app.ui.base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.repository.ChapterRepositoryImpl
import com.shid.mosquefinder.app.ui.main.view_models.ChapterViewModel

class ChapterViewModelFactory(application: Application,chapterRepository: ChapterRepositoryImpl)
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