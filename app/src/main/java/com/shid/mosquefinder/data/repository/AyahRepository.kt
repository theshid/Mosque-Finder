package com.shid.mosquefinder.data.repository

import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.local.database.entities.Ayah

class AyahRepository(private val quranDao: QuranDao) {

    fun getAyah(surahNumber: Int): List<Ayah> {
        return quranDao.getAyah(surahNumber)
    }

    fun updateAyah(text:String,ayahId:Long){
        quranDao.updateAyah(text,ayahId)
    }

    fun getRandomAyah(surahNumber: Int):Ayah{
        return quranDao.getRandomAyah(surahNumber)
    }
}