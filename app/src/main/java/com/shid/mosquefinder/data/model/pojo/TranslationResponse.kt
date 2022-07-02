package com.shid.mosquefinder.data.model.pojo

import com.squareup.moshi.Json

data class TranslationResponse(
    @field:Json(name = "detected_source_language")
    val srcLg: String,
    @field:Json(name = "text")
    val textTranslation: String
)
