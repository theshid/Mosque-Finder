package com.shid.mosquefinder.data.model.pojo

import com.google.gson.annotations.SerializedName

data class VersetResponse(
    @SerializedName("number")
    val surahNumber: Int,
    @SerializedName("englishName")
    val title: String,
    @SerializedName("englishNameTranslation")
    val englishNameTranslation: String,
    @SerializedName("revelationType")
    val revelationType: String,
    @SerializedName("ayahs")
    val verseResponse: List<VerseResponse>
)
