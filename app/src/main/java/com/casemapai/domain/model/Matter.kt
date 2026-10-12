package com.casemapai.domain.model

data class Matter(
    val id: Long = 0L,
    val title: String,
    val summary: String,
    val proceduralPosture: String,
    val disputedFacts: String,
    val notes: String,
    val updatedAtLabel: String = "Just now"
)
