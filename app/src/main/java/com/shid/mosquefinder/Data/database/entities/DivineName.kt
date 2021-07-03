package com.shid.mosquefinder.Data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "noms")
data class DivineName(
    @PrimaryKey(autoGenerate = true)  val id:Int,
    @ColumnInfo(name = "name") val name: String
)
