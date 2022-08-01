package com.shid.mosquefinder.data.repository

import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.local.toDomain
import com.shid.mosquefinder.domain.model.Ayah
import com.shid.mosquefinder.domain.repository.AyahRepository
import javax.inject.Inject

class AyahRepositoryImpl @Inject constructor(private val quranDao: QuranDao) : AyahRepository {

    override fun getAyah(surahNumber: Int): List<Ayah> =
        quranDao.getAyah(surahNumber).map { it.toDomain() }

    override fun updateAyah(text: String, ayahId: Long) {
        quranDao.updateAyah(text, ayahId)
    }

    override fun getRandomAyah(surahNumber: Int): Ayah {
        return quranDao.randomAyah.toDomain()
    }
}


