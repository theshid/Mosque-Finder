package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.domain.model.Surah
import com.shid.mosquefinder.domain.repository.SurahRepository
import kotlinx.coroutines.flow.Flow

typealias GetSurahsForBaseCalculationBaseUseCase = BaseUseCase<Int, Flow<List<Surah>>>

class GetSurahsForBaseCalculationUseCase(private val surahRepository: SurahRepository)
    :GetSurahsForBaseCalculationBaseUseCase{
    override suspend fun invoke(params: Int): Flow<List<Surah>> {
        return surahRepository.getListSurahsForBaseCalculation(params)
    }

}