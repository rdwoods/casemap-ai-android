package com.casemapai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matters")
data class MatterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val summary: String,
    val proceduralPosture: String,
    val disputedFacts: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
)