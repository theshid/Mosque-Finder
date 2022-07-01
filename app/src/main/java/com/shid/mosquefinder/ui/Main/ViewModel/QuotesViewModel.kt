package com.shid.mosquefinder.ui.Main.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.data.model.Quotes
import com.shid.mosquefinder.data.repository.QuoteRepository
import com.shid.mosquefinder.utils.Resource

class QuotesViewModel(var quoteRepository: QuoteRepository) : ViewModel() {

    private var mQuoteMutableList: MutableList<Quotes> = ArrayList()

    init {
        mQuoteMutableList = quoteRepository.getQuotesFromFirebase()

    }

    fun getQuotesFromRepository():MutableList<Quotes>{
        return mQuoteMutableList
    }

    fun retrieveStatusMsg(): LiveData<Resource<String>> {
        return quoteRepository.returnStatusMsg()
    }
}