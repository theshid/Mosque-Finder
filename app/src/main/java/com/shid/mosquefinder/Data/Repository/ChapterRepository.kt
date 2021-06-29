package com.shid.mosquefinder.Data.Repository

import com.shid.mosquefinder.Data.database.QuranDao
import com.shid.mosquefinder.Data.database.entities.Chapter

class ChapterRepository(private val quranDao: QuranDao) {

    fun getChapters(categoryId:Int):List<Chapter>{
        return quranDao.getChapters(categoryId)
    }
}