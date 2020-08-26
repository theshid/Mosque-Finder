package com.shid.mosquefinder.Ui.Main.ViewModel

import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.Data.Model.BeautifulMosques
import com.shid.mosquefinder.Data.Model.Quotes
import com.shid.mosquefinder.Data.Repository.BeautifulMosquesRepository

class BeautifulMosquesViewModel(var beautifulMosquesRepository: BeautifulMosquesRepository): ViewModel() {

    private var mBeautyMutableList: MutableList<BeautifulMosques> = ArrayList()

    init {
        mBeautyMutableList = beautifulMosquesRepository.getMosquesFromFirebase()

    }

    fun getMosquesFromRepository():MutableList<BeautifulMosques>{
        return mBeautyMutableList
    }
}