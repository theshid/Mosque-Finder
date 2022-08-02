package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.domain.model.Verse
import com.shid.mosquefinder.domain.repository.AyahRepository
import kotlinx.coroutines.flow.Flow


typealias GetSurahInFrenchBaseUseCase = BaseUseCase<Int, Flow<List<Verse>>>

class GetSurahInFrenchUseCase(private val ayahRepository: AyahRepository) :
    GetSurahInFrenchBaseUseCase {
    override suspend fun invoke(params: Int): Flow<List<Verse>> {
        return ayahRepository.getSurahInFrench(params)
    }


}