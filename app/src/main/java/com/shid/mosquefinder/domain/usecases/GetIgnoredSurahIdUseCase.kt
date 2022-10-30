package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.app.factory.CrawlerStateSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetIgnoredSurahIdUseCase @Inject constructor(private val store: CrawlerStateSource) {

    suspend fun getIgnoredIds(): List<Long> {
        return store.data.flowOn(Dispatchers.IO).first().ignored.distinct().sorted()
    }
}