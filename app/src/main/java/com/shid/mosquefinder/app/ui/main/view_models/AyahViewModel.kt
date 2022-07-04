package com.shid.mosquefinder.app.ui.main.view_models

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shid.mosquefinder.data.model.pojo.Root
import com.shid.mosquefinder.data.model.pojo.Verse
import com.shid.mosquefinder.data.repository.AyahRepository
import com.shid.mosquefinder.data.repository.SurahRepositoryImpl
import com.shid.mosquefinder.data.local.database.QuranDatabase
import com.shid.mosquefinder.data.local.database.entities.Ayah
import com.shid.mosquefinder.data.local.database.entities.SurahDb
import com.shid.mosquefinder.app.utils.Common
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AyahViewModel(private val application: Application, ayahRepository: AyahRepository) : ViewModel() {
    private val repository = ayahRepository
    val service = Common.frenchQuranApiService

    private var _ayah = MutableLiveData<List<Ayah>>()
    val ayah: LiveData<List<Ayah>>
        get() = _ayah

    private var _surahDb = MutableLiveData<SurahDb>()
    val surahDb: LiveData<SurahDb>
        get() = _surahDb

    private var _listSurahDb = MutableLiveData<List<SurahDb>>()
    val listSurahDb: LiveData<List<SurahDb>>
        get() = _listSurahDb

    var _translation = MutableLiveData<List<Verse>>()
    val translation: LiveData<List<Verse>>
        get() = _translation

    //private var repository: AyahRepository
    private var surahRepositoryImpl: SurahRepositoryImpl


    init {
        val dao =
            QuranDatabase.getDatabase(application, viewModelScope, application.resources).surahDao()
       // repository = AyahRepository(dao)
        surahRepositoryImpl = SurahRepositoryImpl(dao)
    }



    fun getSurahList(surahNumber: Int){
        viewModelScope.launch(Dispatchers.IO) {
            val list = surahRepositoryImpl.getListSurahs(surahNumber)
            _listSurahDb.postValue(list)
        }
    }


    fun getSurahInfo(surahNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val surah = surahRepositoryImpl.getSurahByNumber(surahNumber)
            _surahDb.postValue(surah)
        }
    }

    fun getAllAyah(surahNumber: Int){
        viewModelScope.launch(Dispatchers.IO) {
            val ayahs = repository.getAyah(surahNumber)
            _ayah.postValue(ayahs)
        }
    }

    /*fun fetchFrenchSurah(ayahId:Int){
        repository.getFrenchSurah(ayahId)
    }*/

    fun updateAyah(text:String,id:Long){
        viewModelScope.launch(Dispatchers.IO){
            repository.updateAyah(text,id)
        }

    }

    fun getFrenchSurah(surahId: Int) {
        service.getFrenchSurah(surahId).enqueue(object : Callback<Root> {
            override fun onResponse(call: Call<Root>, response: Response<Root>) {
                if (response.code() == 200) {
                    _translation.value = response.body()!!.data.verse
                    /* GlobalScope.launch(Dispatchers.IO){
                         quranDao.updateAyah(response.body()!!.data.verse,ayahId)
                     }*/

                    Log.d("Ayah", "OnResponse OK : " + response.body()!!.data.verse + " "+surahId)
                } else {

                    Log.d("Ayah", "OnResponse Fail : ")
                }
            }

            override fun onFailure(call: Call<Root>, t: Throwable) {

                Log.d("Ayah", "OnResponse Fail : ")
            }

        })
    }
}