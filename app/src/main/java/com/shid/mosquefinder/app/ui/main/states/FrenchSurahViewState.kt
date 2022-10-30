package com.shid.mosquefinder.app.ui.main.states

import com.shid.mosquefinder.app.ui.models.VersePresentation

data class FrenchSurahViewState(
    val error: Error?,
    val isLoading: Boolean,
    val surahInFrench: List<VersePresentation>?
)
