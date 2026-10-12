package com.casemapai.core

import java.util.UUID

object CurrentUser {
    // Generate this once, paste it in, never change it
    val id: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) UUID = UUID.fromString("cf5a12e8fa6d4ac9abfcbb5ae8c02901")
    val email: String = "rdwoods1@gmail.com"
    val displayName: String = "rdwoods"
}