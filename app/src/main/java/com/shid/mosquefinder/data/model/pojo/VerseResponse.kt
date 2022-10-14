package com.shid.mosquefinder.data.model.pojo

import com.squareup.moshi.Json

data class VerseResponse(
    @field:Json(name = "number")
    val num: Int,
    @field:Json(name = "text")
    val trans: String,
    @field:Json(name = "numberInSurah")
    val numInSurah: Int

)
