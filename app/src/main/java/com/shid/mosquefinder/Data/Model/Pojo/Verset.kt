package com.shid.mosquefinder.Data.Model.Pojo

import com.google.gson.annotations.SerializedName

data class Verset(
    @SerializedName("number")
    val verseNumber:Int,
    @SerializedName("text")
    val verse:String
)
