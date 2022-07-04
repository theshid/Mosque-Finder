package com.shid.mosquefinder.domain.model

data class Surah(
    val number: Int,
    val name: String,
    val transliteration: String,
    val translation: String,
    val totalVerses: Int,
    val revelationType: String
)
