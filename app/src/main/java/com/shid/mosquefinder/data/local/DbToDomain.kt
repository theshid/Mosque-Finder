package com.shid.mosquefinder.data.local

import com.shid.mosquefinder.data.local.database.entities.SurahDb
import com.shid.mosquefinder.domain.model.Surah

internal fun SurahDb.toDomain() = Surah(
    number = this.id,
    name = this.name,
    transliteration = this.transliteration,
    translation = this.translation,
    totalVerses = this.totalVerses,
    revelationType = this.revelationType
)