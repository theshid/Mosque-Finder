package com.shid.mosquefinder.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter")
data class Chapter(
    @PrimaryKey(autoGenerate = true)  val id:Int,
    @ColumnInfo(name = "chapter_name") val chapterName: String,
    @ColumnInfo(name = "category_id") val categoryId: Int,
)
