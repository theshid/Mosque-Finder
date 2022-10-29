package com.shid.mosquefinder.app.ui.main.mappers

import com.shid.mosquefinder.app.ui.models.DeeplPresentation
import com.shid.mosquefinder.app.ui.models.TranslationPresentation
import com.shid.mosquefinder.domain.model.DeepL
import com.shid.mosquefinder.domain.model.Translation

internal fun DeeplPresentation.toDomain() =
    DeepL(translation = this.translation.map { presentation -> presentation.toDomain() })

internal fun TranslationPresentation.toDomain() =
    Translation(sourceLanguage = this.sourceLanguage, textTranslation = this.textTranslation)