package com.shid.mosquefinder.app.ui.main.states

import com.shid.mosquefinder.app.ui.models.AyahPresentation

internal data class AyahViewState(
    val error: Error?,
    val isLoading: Boolean,
    val surahs: List<AyahPresentation>?
)
