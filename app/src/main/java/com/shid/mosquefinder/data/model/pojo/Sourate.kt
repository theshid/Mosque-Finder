package com.shid.mosquefinder.data.model.pojo

import com.google.gson.annotations.SerializedName

data class Sourate(
    @SerializedName("number")
    val surahNumber:Int,
    @SerializedName("name")
    val name:String,
    @SerializedName("englishName")
    val engName:String,
    @SerializedName("englishNameTranslation")
    val engTranslation:String,
    @SerializedName("revelationType")
    val revelationType:String,
    @SerializedName("numberOfAyahs")
    val numAyahs:Int,
    @SerializedName("ayahs")
    val ayahs:List<VersetResponse>
)
