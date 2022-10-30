package com.shid.mosquefinder.app.ui.main.states

import com.shid.mosquefinder.app.ui.models.SurahPresentation
import com.shid.mosquefinder.domain.model.Surah

data class SurahViewState(
    val error: Error?,
    val isLoading:Boolean,
    val surahs:List<SurahPresentation>?
)
