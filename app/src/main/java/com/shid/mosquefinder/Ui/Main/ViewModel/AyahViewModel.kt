package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shid.mosquefinder.Data.Repository.AyahRepository
import com.shid.mosquefinder.Data.database.QuranDatabase
import com.shid.mosquefinder.Data.database.entities.Ayah

class AyahViewModel(private val application: Application) : ViewModel() {
    private var repository: AyahRepository

    init {
        val dao = QuranDatabase.getDatabase(application, viewModelScope, application.resources).surahDao()
        repository = AyahRepository(dao)
    }

    fun getAyah(surahNumber:Int):List<Ayah> = repository.getAyah(surahNumber)
}