package com.shid.mosquefinder.Data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter")
data class Chapter(
    @PrimaryKey(autoGenerate = true)  val id:Long,
    @ColumnInfo(name = "chapter_name") val chapterName: String,
    @ColumnInfo(name = "category_name") val categoryName: String,
)
