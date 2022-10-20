package com.shid.mosquefinder.app.ui.main.states

import com.shid.mosquefinder.data.model.Quotes

data class QuoteViewState(
    val error: Error?,
    val isLoading: Boolean,
    val quotes: List<Quotes>?
)
