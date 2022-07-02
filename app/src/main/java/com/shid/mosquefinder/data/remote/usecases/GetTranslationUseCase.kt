package com.shid.mosquefinder.data.remote.usecases

import com.shid.mosquefinder.domain.AzkharRepository
import com.shid.mosquefinder.domain.model.DeepL
import kotlinx.coroutines.flow.Flow

typealias GetTranslationBaseUseCase = BaseUseCase<String, Flow<DeepL>>

class GetTranslationUseCase(private val azkharRepository: AzkharRepository) :

    GetTranslationBaseUseCase {
    override suspend fun invoke(params: String): Flow<DeepL> {
        return azkharRepository.setTranslation(input = params)
    }

}