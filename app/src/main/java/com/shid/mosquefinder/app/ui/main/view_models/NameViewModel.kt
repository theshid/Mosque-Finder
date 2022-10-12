package com.shid.mosquefinder.app.ui.main.view_models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.models.NameOfAllah
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NameViewModel @Inject constructor(private val application: Application) : ViewModel() {
    lateinit var nameList: List<NameOfAllah>
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