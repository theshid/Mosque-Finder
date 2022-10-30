package com.shid.mosquefinder.domain.repository

import com.shid.mosquefinder.domain.model.DeepL
import kotlinx.coroutines.flow.Flow

interface AzkharRepository {
    suspend fun setTranslation(input: String):Flow<DeepL>
}