package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.app.factory.CrawlerStateSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetLastSurahIdUseCase @Inject constructor(private val store: CrawlerStateSource) {

    suspend fun getLastSurahId(): Long {
        return store.data.flowOn(Dispatchers.IO).first().lastSurahId
    }
}