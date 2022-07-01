package com.shid.mosquefinder.ui.Main.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.data.model.BeautifulMosques
import com.shid.mosquefinder.data.repository.BeautifulMosquesRepository
import com.shid.mosquefinder.utils.Resource

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