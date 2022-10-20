package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.data.model.Quotes
import com.shid.mosquefinder.domain.repository.QuoteRepository

typealias  GetQuotesBaseUseCase = BaseUseCase<Unit, List<Quotes>>

class GetQuotesUseCase(private val quoteRepository: QuoteRepository) : GetQuotesBaseUseCase {
    override suspend fun invoke(params: Unit): List<Quotes> {
        return quoteRepository.getQuotesFromFirebase()
    }
}