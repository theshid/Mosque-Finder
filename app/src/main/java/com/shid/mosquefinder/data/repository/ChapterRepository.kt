package com.shid.mosquefinder.data.repository

import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.local.database.entities.Chapter

class ChapterRepository(private val quranDao: QuranDao) {

    fun getChapters(categoryId:Int):List<Chapter>{
        return quranDao.getChapters(categoryId)
    }
}