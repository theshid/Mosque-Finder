package com.shid.mosquefinder.data.model.Pojo

import com.google.gson.annotations.SerializedName

data class DeepL(
    @SerializedName("translations")
    val translationResponse:List<Translation>
)
