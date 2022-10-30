package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.domain.model.Ayah
import com.shid.mosquefinder.domain.repository.AyahRepository
import kotlinx.coroutines.flow.Flow

typealias GetRandomAyahBaseUseCase = BaseUseCase<Int, Flow<Ayah>>
class GetRandomAyahUseCase(private val ayahRepository: AyahRepository):GetRandomAyahBaseUseCase {
    override suspend fun invoke(params: Int): Flow<Ayah> {
        return ayahRepository.getRandomAyah(params)
    }
}