package com.shid.mosquefinder.app.ui.main.states

import com.shid.mosquefinder.app.ui.models.SurahPresentation

data class SurahByNumberViewState(
    val error: Error?,
    val surah:SurahPresentation?
)
