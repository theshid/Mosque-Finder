package com.shid.mosquefinder.ui.main.mappers

import com.shid.mosquefinder.domain.model.DeepL
import com.shid.mosquefinder.domain.model.Translation
import com.shid.mosquefinder.ui.models.DeeplPresentation
import com.shid.mosquefinder.ui.models.TranslationPresentation

internal fun DeeplPresentation.toDomain() =  DeepL(translation = this.translation.map { presentation -> presentation.toDomain() })

internal fun TranslationPresentation.toDomain() = Translation(sourceLanguage = this.sourceLanguage, textTranslation = this.textTranslation)