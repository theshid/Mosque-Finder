package com.shid.mosquefinder.data.model.pojo

import com.google.gson.annotations.SerializedName

data class VersetResponse(
    @SerializedName("number")
    val verseNumber:Int,
    @SerializedName("ayahs")
    val verseResponse:List<VerseResponse>
)
