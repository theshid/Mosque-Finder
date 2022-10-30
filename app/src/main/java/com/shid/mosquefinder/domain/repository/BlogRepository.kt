package com.shid.mosquefinder.domain.repository

import com.shid.mosquefinder.data.model.Article

interface BlogRepository {

    fun getArticlesFromFirebase(): List<Article>
}