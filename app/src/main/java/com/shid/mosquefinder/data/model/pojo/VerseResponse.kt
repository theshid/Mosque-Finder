package com.shid.mosquefinder.data.model.pojo

import com.google.gson.annotations.SerializedName

data class VerseResponse(
    @SerializedName("number")
    val verseNumber: Int,
    @SerializedName("text")
    val trans: String,
    @SerializedName("numberInSurah")
    val numInSurah: Int

)
