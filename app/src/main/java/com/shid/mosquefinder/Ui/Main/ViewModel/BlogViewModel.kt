package com.shid.mosquefinder.Ui.Main.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.Data.Model.Article
import com.shid.mosquefinder.Data.Repository.BlogRepository
import com.shid.mosquefinder.Utils.Resource

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