package com.shid.mosquefinder.data.remote

import com.shid.mosquefinder.data.model.pojo.DeepLResponse
import com.shid.mosquefinder.data.model.pojo.TranslationResponse
import com.shid.mosquefinder.data.model.pojo.VerseResponse
import com.shid.mosquefinder.domain.model.DeepL
import com.shid.mosquefinder.domain.model.Translation
import com.shid.mosquefinder.domain.model.Verse

internal fun DeepLResponse.toDomain(): DeepL =
    DeepL(this.translationResponse.map { response -> response.toDomain() })

internal fun TranslationResponse.toDomain(): Translation =
    Translation(this.srcLg, this.textTranslation)

internal fun VerseResponse.toDomain(): Verse = Verse(this.verseNumber, this.trans, this.numInSurah)