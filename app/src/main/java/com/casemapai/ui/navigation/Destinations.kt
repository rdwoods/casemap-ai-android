package com.casemapai.ui.navigation

import java.util.UUID

sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object EditMatter : Destination("edit_matter")
    data object MatterDetail : Destination("matter_detail/{matterId}") {
        fun createRoute(matterId: UUID): String = "matter_detail/$matterId"
    }
    data object AnalysisResult : Destination("analysis_result/{matterId}") {
        fun createRoute(matterId: Long): String = "analysis_result/$matterId"
    }
}
