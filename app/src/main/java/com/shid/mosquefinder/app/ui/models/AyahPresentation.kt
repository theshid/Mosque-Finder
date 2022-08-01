package com.shid.mosquefinder.app.ui.models

data class AyahPresentation(
    val id: Long,
    val surah_number: Int,
    val verse_number: Int,
    val originalText: String,
    val translation: String,
    var frenchTranslation: String
)
