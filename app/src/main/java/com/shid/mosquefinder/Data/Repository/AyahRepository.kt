package com.shid.mosquefinder.Data.Repository

import com.shid.mosquefinder.Data.database.QuranDao
import com.shid.mosquefinder.Data.database.QuranDatabase
import com.shid.mosquefinder.Data.database.entities.Ayah

class AyahRepository(private val quranDao: QuranDao) {

    fun getAyah(surahNumber:Int):List<Ayah>{
        return quranDao.getAyah(surahNumber)
    }
}