package com.shid.mosquefinder.domain.repository

import com.shid.mosquefinder.domain.model.Ayah
import com.shid.mosquefinder.domain.model.Verse
import kotlinx.coroutines.flow.Flow

interface AyahRepository {

    fun getAyah(surahNumber: Int): Flow<List<Ayah>>

    suspend fun updateAyah(text: String, ayahId: Long)

    suspend fun getRandomAyah(surahNumber: Int): Flow<Ayah>

    fun getSurahInFrench(surahNumber: Long): Flow<List<Verse>>


}