package com.shid.mosquefinder.app.ui.main.states

import com.shid.mosquefinder.data.model.BeautifulMosques

data class BeautyMosqueViewState(
    val error: Error?,
    val isLoading: Boolean,
    val mosques: List<BeautifulMosques>?
)
