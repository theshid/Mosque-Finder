package com.shid.mosquefinder.app.ui.main.states

import com.shid.mosquefinder.data.model.Article

internal data class BlogViewState(
    val error:Error?,
    val isLoading:Boolean,
    val articles:List<Article>?
)
