package com.shid.mosquefinder.Data.Repository

import androidx.lifecycle.LiveData
import com.shid.mosquefinder.Data.database.QuranDao
import com.shid.mosquefinder.Data.database.entities.Surah

class SurahRepository(private val quranDao: QuranDao) {

    suspend fun getAllSurahs():List<Surah>{
        return quranDao.getSurahs()
    }

    suspend fun getSurahByNumber(surahNumber:Int):Surah = quranDao.getSurahByNumber(surahNumber)
}