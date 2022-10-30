package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.app.factory.CrawlerStateSource
import javax.inject.Inject

class RegisterIgnoredSurahIdUseCase @Inject constructor(private val store: CrawlerStateSource) {

    suspend fun addIgnored(surahId: Long) {
        store.updateData { current -> current.copy(ignored = current.ignored + surahId) }
    }
}