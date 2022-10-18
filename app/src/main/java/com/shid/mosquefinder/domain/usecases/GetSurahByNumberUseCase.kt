package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.domain.model.Surah
import com.shid.mosquefinder.domain.repository.SurahRepository
import kotlinx.coroutines.flow.Flow

typealias GetSurahByNumberBaseUseCase = BaseUseCase<Int, Flow<Surah>>

class GetSurahByNumberUseCase(private val surahRepository: SurahRepository) :
    GetSurahByNumberBaseUseCase {
    override suspend fun invoke(params: Int): Flow<Surah> {
        return surahRepository.getSurahByNumber(params)
    }

}