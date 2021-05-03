package com.shid.mosquefinder.Data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.shid.mosquefinder.Data.database.entities.Ayah
import com.shid.mosquefinder.Data.database.entities.Surah

@Dao
interface QuranDao {
    @Insert
    suspend fun insertSurah(vararg surah: Surah)

    @Insert
    suspend fun insertAyah(vararg ayah: Ayah)

    @get:Query("SELECT * FROM surahs ORDER BY RANDOM() LIMIT 1")
    val randomSurah: Surah

    @get:Query("SELECT * FROM ayahs ORDER BY RANDOM() LIMIT 1")
    val randomAyah: Ayah

    @Query("SELECT * FROM surahs ORDER BY number")
    fun getSurahs():List<Surah>

    @Query("SELECT * FROM ayahs WHERE surah_number IN (:surahNumber)")
    fun getAyah(surahNumber:Int):List<Ayah>
}