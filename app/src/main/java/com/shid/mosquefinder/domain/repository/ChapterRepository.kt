package com.shid.mosquefinder.domain.repository

import com.shid.mosquefinder.data.local.database.entities.Chapter

interface ChapterRepository {
    fun getChapters(categoryId:Int):List<Chapter>
}