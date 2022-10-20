package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.data.model.BeautifulMosques
import com.shid.mosquefinder.domain.repository.BeautifulMosquesRepository

typealias GetBeautifulMosquesBaseUseCase = BaseUseCase<Unit, List<BeautifulMosques>>

class GetBeautifulMosquesUseCase(private val mosquesRepository: BeautifulMosquesRepository) :
    GetBeautifulMosquesBaseUseCase {
    override suspend fun invoke(params: Unit): List<BeautifulMosques> {
        return mosquesRepository.getMosquesFromFirebase()
    }
}