package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.app.factory.CrawlerStateSource
import javax.inject.Inject

class SaveLastSurahIdUseCase @Inject constructor(private val store: CrawlerStateSource) {

    suspend fun saveLastSurahId(value: Long): Long {
        return store.updateData { current -> current.copy(lastSurahId = value) }.lastSurahId
    }
}