package com.shid.mosquefinder.ui.main.mappers

import com.shid.mosquefinder.domain.model.DeepL
import com.shid.mosquefinder.domain.model.Translation
import com.shid.mosquefinder.ui.models.DeeplPresentation
import com.shid.mosquefinder.ui.models.TranslationPresentation

internal fun DeepL.toPresentation() =  DeeplPresentation(translation = this.translation.map { presentation -> presentation.toPresentation() })

internal fun Translation.toPresentation() = TranslationPresentation(sourceLanguage = this.sourceLanguage, textTranslation = this.textTranslation)