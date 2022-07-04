package com.shid.mosquefinder.app.ui.models

data class SurahPresentation(
    val number: Int,
    val name: String,
    val transliteration: String,
    val translation: String,
    val totalVerses: Int,
    val revelationType: String
)
