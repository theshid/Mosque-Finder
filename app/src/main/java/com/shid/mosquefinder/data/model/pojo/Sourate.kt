package com.shid.mosquefinder.data.model.pojo

import com.squareup.moshi.Json

data class Sourate(
    @field:Json(name = "number")
    val surahNumber: Int,
    @field:Json(name = "name")
    val name: String,
    @field:Json(name = "englishName")
    val engName: String,
    @field:Json(name = "englishNameTranslation")
    val engTranslation: String,
    @field:Json(name = "revelationType")
    val revelationType: String,
    @field:Json(name = "numberOfAyahs")
    val numAyahs: Int,
    @field:Json(name = "ayahs")
    val ayahs: List<VersetResponse>
)
