package com.shid.mosquefinder.domain.repository

import com.shid.mosquefinder.data.model.Quotes

interface QuoteRepository {

    fun getQuotesFromFirebase(): MutableList<Quotes>
}