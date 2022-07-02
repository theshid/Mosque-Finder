package com.shid.mosquefinder.data.model.pojo

import com.squareup.moshi.Json

data class DeepLResponse(
    @field:Json(name = "translations")
    val translationResponse: List<TranslationResponse>
)
