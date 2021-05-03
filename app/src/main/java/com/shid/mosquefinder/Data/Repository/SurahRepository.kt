package com.shid.mosquefinder.Data.Repository

import com.shid.mosquefinder.Data.database.QuranDao
import com.shid.mosquefinder.Data.database.entities.Surah

class SurahRepository(private val quranDao: QuranDao) {

    fun getAllSurahs():List<Surah>{
        return quranDao.getSurahs()
    }
}