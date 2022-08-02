package com.shid.mosquefinder.app.ui.main.mappers

import com.shid.mosquefinder.app.ui.models.*
import com.shid.mosquefinder.app.ui.models.DeeplPresentation
import com.shid.mosquefinder.app.ui.models.TranslationPresentation
import com.shid.mosquefinder.domain.model.*

internal fun DeepL.toPresentation() =
    DeeplPresentation(translation = this.translation.map { presentation -> presentation.toPresentation() })

internal fun Translation.toPresentation() = TranslationPresentation(
    sourceLanguage = this.sourceLanguage,
    textTranslation = this.textTranslation
)

internal fun Surah.toPresentation() = SurahPresentation(
    number = this.number,
    name = this.name,
    transliteration = this.transliteration,
    translation = this.translation,
    totalVerses = this.totalVerses,
    revelationType = this.revelationType
)

internal fun Ayah.toPresentation() = AyahPresentation(
    id, surah_number, verse_number, originalText, translation, frenchTranslation
)

internal fun Verse.toPresentation() = VersePresentation(num, trans, numInSurah)