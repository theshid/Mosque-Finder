package com.shid.mosquefinder.Data.Model.Pojo

import com.google.gson.annotations.SerializedName

data class Verset(
    @SerializedName("number")
    val verseNumber:Int,
    @SerializedName("text")
    val verse:String,
    @SerializedName("numberInSurah")
    val numInSurah:Int,
    @SerializedName("juz")
    val juz:Int,
    @SerializedName("manzil")
    val manzil:Int,
    @SerializedName("page")
    val page:Int,
    @SerializedName("ruku")
    val ruku:Int,
    @SerializedName("hizbQuarter")
    val hizb:Int,
    @SerializedName("sajda")
    val sajda:Boolean
)
