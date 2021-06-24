package com.shid.mosquefinder.Data.Model.Pojo

import com.google.gson.annotations.SerializedName

data class Verse(
    @SerializedName("number")
    val num:Int,
    @SerializedName("text")
    val trans:String,
    @SerializedName("numberInSurah")
    val numInSurah:Int

)
