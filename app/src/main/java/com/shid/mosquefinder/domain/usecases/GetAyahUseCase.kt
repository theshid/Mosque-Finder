package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.domain.model.Ayah
import com.shid.mosquefinder.domain.repository.AyahRepository
import kotlinx.coroutines.flow.Flow

typealias GetAllAyahBaseUseCase = BaseUseCase<Int, List<Ayah>>

class GetAyahUseCase(private val ayahRepository: AyahRepository) : GetAllAyahBaseUseCase {
    override suspend fun invoke(params: Int): List<Ayah> {
        return ayahRepository.getAyah(params)
    }

}