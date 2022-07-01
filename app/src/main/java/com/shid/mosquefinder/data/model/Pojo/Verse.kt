package com.shid.mosquefinder.data.model.Pojo

import com.google.gson.annotations.SerializedName

data class Verse(
    @SerializedName("number")
    val num:Int,
    @SerializedName("text")
    val trans:String,
    @SerializedName("numberInSurah")
    val numInSurah:Int

)
