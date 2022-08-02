package com.shid.mosquefinder.data.model.pojo

import com.squareup.moshi.Json

data class RootResponse(
    @field:Json(name = "data")
    val data: VersetResponse,
    )
