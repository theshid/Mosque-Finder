package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shid.mosquefinder.Data.Repository.AyahRepository
import com.shid.mosquefinder.Data.Repository.SurahRepository
import com.shid.mosquefinder.Data.database.QuranDatabase
import com.shid.mosquefinder.Data.database.entities.Ayah
import com.shid.mosquefinder.Data.database.entities.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AyahViewModel(private val application: Application) : ViewModel() {
    private var _ayah = MutableLiveData<List<Ayah>>()
    val ayah: LiveData<List<Ayah>>
        get() = _ayah

    private var _surah = MutableLiveData<Surah>()
    val surah: LiveData<Surah>
        get() = _surah

    private var repository: AyahRepository
    private var surahRepository: SurahRepository

    init {
        val dao =
            QuranDatabase.getDatabase(application, viewModelScope, application.resources).surahDao()
        repository = AyahRepository(dao)
        surahRepository = SurahRepository(dao)
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
}