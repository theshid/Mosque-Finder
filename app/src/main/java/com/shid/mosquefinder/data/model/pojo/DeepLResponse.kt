package com.shid.mosquefinder.data.model.pojo

import com.google.gson.annotations.SerializedName

data class DeepLResponse(
    @SerializedName("translations")
    val translationResponse: List<TranslationResponse>
)
