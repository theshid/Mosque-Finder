package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shid.mosquefinder.Data.Repository.AzkharRepository

class AzkharViewModel(azkharRepository: AzkharRepository,application: Application) :ViewModel(){
    private val repository = azkharRepository
    var output = repository.translation
    fun setTranslation(input:String){
        repository.setTranslation(input)
    }
}