package com.shid.mosquefinder.data.repository

import com.shid.mosquefinder.data.database.QuranDao
import com.shid.mosquefinder.data.database.entities.Chapter

class ChapterRepository(private val quranDao: QuranDao) {

    fun getChapters(categoryId:Int):List<Chapter>{
        return quranDao.getChapters(categoryId)
    }
}