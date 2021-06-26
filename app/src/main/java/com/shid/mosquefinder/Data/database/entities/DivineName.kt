package com.shid.mosquefinder.Data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class DivineName(
    @PrimaryKey(autoGenerate = true)  val id:Long,
    @ColumnInfo(name = "name") val name: String
)
