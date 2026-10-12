package com.casemapai.domain.prompt

import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.domain.model.Matter

class IssueSpotterPromptBuilder {

    fun build(matter: GetMatterQuery.Data.Matter): String {
        return """
            You are a legal analysis assistant helping with internal brainstorming based only on sanitized facts.

            Rules:
            1. Do not invent facts.
            2. If a key fact is missing, say so.
            3. Be structured and concise.
            4. Do not provide public-facing legal advice.
            5. Use only the information below.

            Return the response with these exact headings:
            - Potential Legal Issues
            - Why Each Issue Matters
            - Missing Facts Needed
            - Government-Favorable Points
            - Potential Weaknesses

            Matter Title:
            ${matter.title}

            Sanitized Summary:
            ${matter.summary}

            Procedural Posture:
            ${matter.proceduralPosture}

            Disputed Facts:
            ${matter.disputedFacts}

            Notes:
            ${matter.notes}
        """.trimIndent()
    }
}