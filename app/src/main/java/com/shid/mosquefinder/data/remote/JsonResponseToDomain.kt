package com.shid.mosquefinder.data.remote

import com.shid.mosquefinder.data.model.pojo.DeepLResponse
import com.shid.mosquefinder.data.model.pojo.TranslationResponse
import com.shid.mosquefinder.domain.model.DeepL
import com.shid.mosquefinder.domain.model.Translation

internal fun DeepLResponse.toDomain():DeepL = DeepL(this.translationResponse.map { response -> response.toDomain() })

internal fun TranslationResponse.toDomain():Translation = Translation(this.srcLg,this.textTranslation)