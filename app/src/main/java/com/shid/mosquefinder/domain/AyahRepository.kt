package com.shid.mosquefinder.domain

import com.shid.mosquefinder.data.database.entities.Ayah

interface AyahRepository {

    fun getAyah(surahNumber: Int): List<Ayah>

    fun updateAyah(text:String,ayahId:Long)

    fun getRandomAyah(surahNumber: Int)
}