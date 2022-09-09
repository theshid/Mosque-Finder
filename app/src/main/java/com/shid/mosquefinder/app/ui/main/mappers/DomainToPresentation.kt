package com.shid.mosquefinder.app.ui.main.mappers

import com.shid.mosquefinder.app.ui.models.*
import com.shid.mosquefinder.data.model.Article
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

internal fun Article.toPresentation() =
    ArticlePresentation(title = this.title, title_fr = this.title_fr, author = this.author,
    body = this.body, pic = this.pic, body_fr = this.body_fr, tag = this.tag)