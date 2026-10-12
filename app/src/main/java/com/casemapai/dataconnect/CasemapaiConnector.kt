package com.casemapai.dataconnect

import com.google.firebase.Timestamp
import java.util.UUID

object CasemapaiConnector {
    val instance: CasemapaiConnector = CasemapaiConnector

    val getUser = GetUserQuery
    val listMatters = ListMattersQuery
    val getMatter = GetMatterQuery
    val createMatter = CreateMatterMutation
    val listAnalysisResults = ListAnalysisResultsQuery
}

object GetUserQuery {
    fun execute(id: UUID): Response {
        return Response(Data(Data.User(id, "rdwoods1@gmail.com", "rdwoods")))
    }
    data class Response(val data: Data)
    data class Data(val user: User?) {
        data class User(
            val id: UUID,
            val email: String,
            val displayName: String
        )
    }
}

object ListMattersQuery {
    fun execute(userId: UUID): Response {
        return Response(Data(emptyList()))
    }
    data class Response(val data: Data)
    data class Data(val matters: List<MattersItem>) {
        data class MattersItem(
            val id: UUID,
            val title: String,
            val summary: String,
            val proceduralPosture: String,
            val disputedFacts: String,
            val notes: String,
            val createdAt: Timestamp,
            val updatedAt: Timestamp
        )
    }
}

object GetMatterQuery {
    fun execute(id: UUID): Response {
        return Response(Data(null))
    }
    data class Response(val data: Data)
    data class Data(val matter: Matter?) {
        data class Matter(
            val user: GetUserQuery.Data.User,
            val id: UUID,
            val title: String,
            val summary: String,
            val proceduralPosture: String,
            val disputedFacts: String,
            val notes: String,
            val createdAt: Timestamp,
            val updatedAt: Timestamp
        )
    }
}

object CreateMatterMutation {
    fun execute(
        userId: UUID,
        title: String,
        summary: String,
        proceduralPosture: String,
        disputedFacts: String,
        createdAt: Timestamp,
        updatedAt: Timestamp
    ): Response {
        return Response(Data(Data.MatterInsert(UUID.randomUUID())))
    }
    data class Response(val data: Data)
    data class Data(val matter_insert: MatterInsert) {
        data class MatterInsert(val id: UUID)
    }
}

object ListAnalysisResultsQuery {
    fun execute(matterId: UUID): Response {
        return Response(Data(emptyList()))
    }
    data class Response(val data: Data)
    data class Data(val analysisResults: List<AnalysisResultsItem>) {
        data class AnalysisResultsItem(
            val id: UUID,
            val matterId: UUID,
            val analysisType: String,
            val body: String,
            val createdAt: Timestamp
        )
    }
}
