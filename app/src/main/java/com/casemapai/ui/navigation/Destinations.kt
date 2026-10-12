package com.casemapai.ui.navigation

import java.util.UUID

sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object EditMatter : Destination("edit_matter")
    /** Optional caseId = continue an existing matter's interview. */
    data object Interview : Destination("interview?caseId={caseId}") {
        fun createRoute(caseId: UUID? = null): String = if (caseId == null) "interview" else "interview?caseId=$caseId"
    }
    /** String arg on purpose: UUIDs don't fit NavType.LongType (the bug in MatterDetail below). */
    data object CaseFile : Destination("case_file/{caseId}") {
        fun createRoute(caseId: UUID): String = "case_file/$caseId"
    }
    data object MatterDetail : Destination("matter_detail/{matterId}") {
        fun createRoute(matterId: UUID): String = "matter_detail/$matterId"
    }
    data object AnalysisResult : Destination("analysis_result/{matterId}") {
        fun createRoute(matterId: Long): String = "analysis_result/$matterId"
    }
}
