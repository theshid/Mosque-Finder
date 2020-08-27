package com.shid.mosquefinder.Ui.Main.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.Data.Model.BeautifulMosques
import com.shid.mosquefinder.Data.Model.Quotes
import com.shid.mosquefinder.Data.Repository.BeautifulMosquesRepository
import com.shid.mosquefinder.Utils.Resource

class BeautifulMosquesViewModel(var beautifulMosquesRepository: BeautifulMosquesRepository): ViewModel() {

    private var mBeautyMutableList: MutableList<BeautifulMosques> = ArrayList()

    init {
        mBeautyMutableList = beautifulMosquesRepository.getMosquesFromFirebase()

    }

    fun getMosquesFromRepository():MutableList<BeautifulMosques>{
        return mBeautyMutableList
    }

    fun retrieveStatusMsg(): LiveData<Resource<String>> {
        return beautifulMosquesRepository.returnStatusMsg()
    }
}