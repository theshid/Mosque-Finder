package com.shid.mosquefinder.data.model.pojo

import com.squareup.moshi.Json

data class Root(
    @field:Json(name = "data")
    val data: Verset,
    )
