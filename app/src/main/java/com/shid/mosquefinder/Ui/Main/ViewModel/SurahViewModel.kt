package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shid.mosquefinder.Data.Repository.SurahRepository
import com.shid.mosquefinder.Data.database.QuranDatabase
import com.shid.mosquefinder.Data.database.entities.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SurahViewModel(private val application: Application) : ViewModel() {
    private var _list = MutableLiveData<List<Surah>>()
    val surahList: LiveData<List<Surah>>
        get() = _list
    private var repository: SurahRepository


    init {
        val quranDao = QuranDatabase.getDatabase(application, viewModelScope, application.resources)
            .surahDao()
        repository = SurahRepository(quranDao)
        /*viewModelScope.launch(Dispatchers.IO) {
            listSurahs = repository.getAllSurahs()
        }*/
        //listSurahs = repository.getAllSurahs()
    }
    fun getSurahs(){
        viewModelScope.launch(Dispatchers.IO){
            val listSurah = repository.getAllSurahs()
            _list.postValue(listSurah)
        }
    }

  
}