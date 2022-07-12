package com.shid.mosquefinder.app.ui.main.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.data.model.Quotes
import com.shid.mosquefinder.data.repository.QuoteRepositoryImpl
import com.shid.mosquefinder.app.utils.helper_class.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuotesViewModel @Inject constructor(var quoteRepositoryImpl: QuoteRepositoryImpl) : ViewModel() {

    private val mQuoteMutableList: MutableList<Quotes> = ArrayList()

    init {
        mQuoteMutableList.addAll(quoteRepositoryImpl.getQuotesFromFirebase())

    }

    fun getQuotesFromRepository():MutableList<Quotes>{
        return mQuoteMutableList
    }

    fun retrieveStatusMsg(): LiveData<Resource<String>> {
        return quoteRepositoryImpl.returnStatusMsg()
    }
}