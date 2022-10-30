package com.shid.mosquefinder.domain.model

data class Ayah(
    val id: Long,
    val surah_number: Int,
    val verse_number: Int,
    val originalText: String,
    val translation: String,
    var frenchTranslation: String
)
