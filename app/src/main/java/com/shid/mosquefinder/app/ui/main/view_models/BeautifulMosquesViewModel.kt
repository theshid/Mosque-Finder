package com.shid.mosquefinder.app.ui.main.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.data.model.BeautifulMosques
import com.shid.mosquefinder.data.repository.BeautifulMosquesRepository
import com.shid.mosquefinder.app.utils.helper_class.Resource

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