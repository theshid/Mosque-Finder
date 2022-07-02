package com.shid.mosquefinder.ui.main.states

import com.shid.mosquefinder.ui.models.DeeplPresentation
import com.shid.mosquefinder.ui.models.TranslationPresentation

internal data class AzkharViewState(
    val isComplete: Boolean,
    val error: Error?,
    val translation:DeeplPresentation?
)
