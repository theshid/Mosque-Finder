package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.models.NameOfAllah
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.coroutines.launch

class NameViewModel(private val application: Application) : ViewModel() {
    lateinit var nameList :List<NameOfAllah>
    private var repository: MuslimRepository

    init {
        repository = MuslimRepository(application.baseContext)
    }


    fun getListName() {
        viewModelScope.launch {
            nameList = repository.getNamesOfAllah(Language.EN)
        }

    }
}