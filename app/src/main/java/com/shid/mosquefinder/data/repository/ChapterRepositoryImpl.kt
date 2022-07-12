package com.shid.mosquefinder.data.repository

import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.local.database.entities.Chapter
import com.shid.mosquefinder.domain.repository.ChapterRepository
import javax.inject.Inject

class ChapterRepositoryImpl @Inject constructor(private val quranDao: QuranDao): ChapterRepository {

    override fun getChapters(categoryId:Int):List<Chapter>{
        return quranDao.getChapters(categoryId)
    }
}