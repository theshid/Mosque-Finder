package com.shid.mosquefinder.domain.usecases

import com.shid.mosquefinder.data.model.Article
import com.shid.mosquefinder.domain.repository.BlogRepository
import kotlinx.coroutines.flow.Flow

typealias GetArticlesBaseUseCase = BaseUseCase<Unit, List<Article>>

class GetArticlesUseCase(private val blogRepository: BlogRepository) : GetArticlesBaseUseCase {
    override suspend fun invoke(params: Unit): List<Article> {
        return blogRepository.getArticlesFromFirebase()
    }

}