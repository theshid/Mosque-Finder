package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shid.mosquefinder.Data.Model.Pojo.Root
import com.shid.mosquefinder.Data.Model.Pojo.Verse
import com.shid.mosquefinder.Data.Repository.AyahRepository
import com.shid.mosquefinder.Data.Repository.SurahRepository
import com.shid.mosquefinder.Data.database.QuranDatabase
import com.shid.mosquefinder.Data.database.entities.Ayah
import com.shid.mosquefinder.Data.database.entities.Surah
import com.shid.mosquefinder.Utils.Common
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

    private var _surah = MutableLiveData<Surah>()
    val surah: LiveData<Surah>
        get() = _surah

    private var _listSurah = MutableLiveData<List<Surah>>()
    val listSurah: LiveData<List<Surah>>
        get() = _listSurah

    var _translation = MutableLiveData<List<Verse>>()
    val translation: LiveData<List<Verse>>
        get() = _translation

    //private var repository: AyahRepository
    private var surahRepository: SurahRepository


    init {
        val dao =
            QuranDatabase.getDatabase(application, viewModelScope, application.resources).surahDao()
       // repository = AyahRepository(dao)
        surahRepository = SurahRepository(dao)
    }



    fun getSurahList(surahNumber: Int){
        viewModelScope.launch(Dispatchers.IO) {
            val list = surahRepository.getListSurahs(surahNumber)
            _listSurah.postValue(list)
        }
    }


    fun getSurahInfo(surahNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val surah = surahRepository.getSurahByNumber(surahNumber)
            _surah.postValue(surah)
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