package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.domain.model.Surah
import com.shid.mosquefinder.domain.repository.SurahRepository
import kotlinx.coroutines.flow.Flow

typealias GetAllSurahsBaseUseCase = BaseUseCase<Unit, Flow<List<Surah>>>

class GetAllSurahsUseCase(private val surahRepository: SurahRepository) : GetAllSurahsBaseUseCase {
    override suspend fun invoke(params: Unit): Flow<List<Surah>> {
        return surahRepository.getAllSurahs()
    }

}