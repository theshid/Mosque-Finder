package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shid.mosquefinder.Data.Repository.SurahRepository
import com.shid.mosquefinder.Data.database.QuranDatabase
import com.shid.mosquefinder.Data.database.entities.Surah
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.SharePref
import com.shid.mosquefinder.Utils.doAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

    fun update(){
        doAsync {
            val context = application.applicationContext
            val pushId = runBlocking { getPushId(context) }
        }
    }

    private suspend fun getPushId(context: Context):String?{
        val sharePref = SharePref(context)
        val pushId = sharePref.loadFirebaseToken()
        return if (pushId.isNullOrBlank()){
            Common.retrievePushId(context)
        }else{
            pushId
        }
    }

  
}