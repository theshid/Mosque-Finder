package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shid.mosquefinder.Data.Repository.SurahRepository
import com.shid.mosquefinder.Data.database.QuranDatabase
import com.shid.mosquefinder.Data.database.entities.Surah

class SurahViewModel(private val application: Application) : ViewModel() {

    private var repository: SurahRepository
    var listSurahs:List<Surah>

    init {
        val quranDao = QuranDatabase.getDatabase(application, viewModelScope, application.resources)
            .surahDao()
        repository = SurahRepository(quranDao)
        listSurahs = repository.getAllSurahs()

    }
}