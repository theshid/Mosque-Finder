package com.shid.mosquefinder.Data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class Surah(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "number") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "transliteration_en") val transliteration: String,
    @ColumnInfo(name = "translation_en") val translation: String,
    @ColumnInfo(name = "total_verses") val totalVerses: Int,
    @ColumnInfo(name = "revelation_type") val revelationType: String
)
