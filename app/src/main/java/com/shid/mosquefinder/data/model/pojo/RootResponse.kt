package com.shid.mosquefinder.data.model.pojo

import com.google.gson.annotations.SerializedName

data class RootResponse(
    @SerializedName("data")
    val data: VersetResponse,
)
