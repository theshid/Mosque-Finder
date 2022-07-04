package com.shid.mosquefinder.app.ui.main.view_models

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shid.mosquefinder.data.repository.ChapterRepository
import com.shid.mosquefinder.data.local.database.entities.Chapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ChapterViewModel(val application: Application,val repository: ChapterRepository):ViewModel() {
    private var _chapters = MutableLiveData<List<Chapter>>()
    val chapters: LiveData<List<Chapter>>
        get() = _chapters


    fun fetchChapters(categoryId:Int){
        viewModelScope.launch(Dispatchers.IO){
           val list =  repository.getChapters(categoryId)
            Timber.d("list$list")
            _chapters.postValue(list)
        }
    }
}