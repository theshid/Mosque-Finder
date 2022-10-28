package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.domain.model.Verse
import com.shid.mosquefinder.domain.repository.AyahRepository
import kotlinx.coroutines.flow.Flow


typealias GetSurahInFrenchBaseUseCase = BaseUseCase<Long, Flow<List<Verse>>>

class GetSurahInFrenchUseCase(private val ayahRepository: AyahRepository) :
    GetSurahInFrenchBaseUseCase {
    override suspend fun invoke(params: Long): Flow<List<Verse>> {
        return ayahRepository.getSurahInFrench(params)
    }


}