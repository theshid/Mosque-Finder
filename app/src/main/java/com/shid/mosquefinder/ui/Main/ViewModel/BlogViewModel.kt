package com.shid.mosquefinder.ui.Main.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.data.model.Article
import com.shid.mosquefinder.data.repository.BlogRepository
import com.shid.mosquefinder.utils.Resource

class BlogViewModel(val repository: BlogRepository) : ViewModel() {
    private var mBlogMutableList: MutableList<Article> = ArrayList()

    init {
        mBlogMutableList = repository.getArticlesFromFirebase()

    }

    fun getArticlesFromRepository(): MutableList<Article> {
        return mBlogMutableList
    }

    fun retrieveStatusMsg(): LiveData<Resource<String>> {
        return repository.returnStatusMsg()
    }
}