package com.shid.mosquefinder.Ui.Main.ViewModel

import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.Data.Model.Quotes
import com.shid.mosquefinder.Data.Repository.QuoteRepository

class QuotesViewModel(var quoteRepository: QuoteRepository) : ViewModel() {

    private var mQuoteMutableList: MutableList<Quotes> = ArrayList()

    init {
        mQuoteMutableList = quoteRepository.getQuotesFromFirebase()

    }

    fun getQuotesFromRepository():MutableList<Quotes>{
        return mQuoteMutableList
    }
}