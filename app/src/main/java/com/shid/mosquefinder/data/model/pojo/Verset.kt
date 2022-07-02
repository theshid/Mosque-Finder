package com.shid.mosquefinder.data.model.pojo

import com.google.gson.annotations.SerializedName

data class Verset(
    @SerializedName("number")
    val verseNumber:Int,
    @SerializedName("ayahs")
    val verse:List<Verse>
)
