package com.shid.mosquefinder.data.model.pojo

import com.squareup.moshi.Json

data class VersetResponse(
    @field:Json(name = "number")
    val verseNumber: Int,
    @field:Json(name = "englishName")
    val englishName: String,
    @field:Json(name = "englishNameTranslation")
    val englishNameTranslation: String,
    @field:Json(name = "revelationType")
    val revelationType: String,
    @field:Json(name = "ayahs")
    val verseResponse: List<VerseResponse>
)
