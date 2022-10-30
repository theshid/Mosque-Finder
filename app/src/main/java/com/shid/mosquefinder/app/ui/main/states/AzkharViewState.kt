package com.shid.mosquefinder.app.ui.main.states

import com.shid.mosquefinder.app.ui.models.DeeplPresentation

internal data class AzkharViewState(
    val isComplete: Boolean,
    val error: Error?,
    val translation:DeeplPresentation?
)
