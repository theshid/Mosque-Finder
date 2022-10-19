package com.shid.mosquefinder.domain.repository

import com.shid.mosquefinder.domain.model.Ayah
import com.shid.mosquefinder.domain.model.Verse
import kotlinx.coroutines.flow.Flow

interface AyahRepository {

    fun getAyah(surahNumber: Int): Flow<List<Ayah>>

    suspend fun updateAyah(text: String, ayahId: Long)

    fun getRandomAyah(surahNumber: Int): Ayah

    fun getSurahInFrench(surahNumber: Int): Flow<List<Verse>>


}