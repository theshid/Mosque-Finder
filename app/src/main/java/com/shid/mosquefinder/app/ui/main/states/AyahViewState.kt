package com.shid.mosquefinder.app.ui.main.states

import com.shid.mosquefinder.app.ui.models.AyahPresentation

data class AyahViewState(
    val error: Error?,
    val isLoading: Boolean,
    val ayahs: List<AyahPresentation>?
)
