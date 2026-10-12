package com.casemapai.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "analysis_results",
    foreignKeys = [
        ForeignKey(
            entity = MatterEntity::class,
            parentColumns = ["id"],
            childColumns = ["matterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("matterId")]
)
data class AnalysisResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matterId: UUID,
    val analysisType: String,
    val promptUsed: String,
    val rawResponse: String,
    val createdAt: Long
)