package com.shid.mosquefinder.app.ui.main.states

import com.shid.mosquefinder.app.ui.models.SurahPresentation

data class SurahsForBaseCalculationViewState(
    val error: Error?,
    val surahs:List<SurahPresentation>?
)
