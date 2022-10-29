package com.shid.mosquefinder.data.model.pojo

import com.google.gson.annotations.SerializedName

data class TranslationResponse(
    @SerializedName("detected_source_language")
    val srcLg: String,
    @SerializedName("text")
    val textTranslation: String
)
