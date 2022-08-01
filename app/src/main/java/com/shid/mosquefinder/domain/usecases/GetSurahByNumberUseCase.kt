package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.domain.model.Surah
import com.shid.mosquefinder.domain.repository.SurahRepository

typealias GetSurahByNumberBaseUseCase = BaseUseCase<Int, Surah>

class GetSurahByNumberUseCase(private val surahRepository: SurahRepository) :
    GetSurahByNumberBaseUseCase {
    override suspend fun invoke(params: Int): Surah {
        return surahRepository.getSurahByNumber(params)
    }

}