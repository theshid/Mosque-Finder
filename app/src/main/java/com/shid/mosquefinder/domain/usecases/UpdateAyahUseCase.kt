package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.domain.repository.AyahRepository
import com.shid.mosquefinder.domain.repository.SurahRepository

typealias UpdateAyahBaseUseCase = BaseUseCase<Pair<String,Long>, Unit>

class UpdateAyahUseCase(private val ayahRepository: AyahRepository) : UpdateAyahBaseUseCase {
    override suspend fun invoke(params: Pair<String,Long>) {
        return ayahRepository.updateAyah(params.first,params.second)
    }

}