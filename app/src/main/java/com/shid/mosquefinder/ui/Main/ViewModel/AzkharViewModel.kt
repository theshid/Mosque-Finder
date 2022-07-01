package com.shid.mosquefinder.ui.Main.ViewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.data.repository.AzkharRepository

class AzkharViewModel(azkharRepository: AzkharRepository,application: Application) :ViewModel(){
    private val repository = azkharRepository
    var output = repository.translation
    fun setTranslation(input:String){
        repository.setTranslation(input)
    }
}