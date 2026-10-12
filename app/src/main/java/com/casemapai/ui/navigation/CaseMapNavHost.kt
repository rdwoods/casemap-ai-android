package com.casemapai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.casemapai.ui.analysis.AnalysisResultScreen
import com.casemapai.ui.analysis.AnalysisViewModel
import com.casemapai.ui.casefile.CaseFileScreen
import com.casemapai.ui.casefile.CaseFileViewModel
import com.casemapai.ui.home.HomeScreen
import com.casemapai.ui.home.HomeViewModel
import com.casemapai.ui.interview.InterviewScreen
import com.casemapai.ui.interview.InterviewViewModel
import com.casemapai.ui.matter.EditMatterScreen
import com.casemapai.ui.matter.EditMatterViewModel
import com.casemapai.ui.matter.MatterDetailScreen
import com.casemapai.ui.matter.MatterDetailViewModel

@Composable
fun CaseMapNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier
    ) {
        composable(Destination.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onStartInterview = { navController.navigate(Destination.Interview.createRoute()) },
                onOpenMatter = { caseId ->
                    navController.navigate(Destination.CaseFile.createRoute(caseId))
                }
            )
        }

        composable(
            route = Destination.Interview.route,
            arguments = listOf(
                navArgument(InterviewViewModel.ARG_CASE_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: InterviewViewModel = hiltViewModel()
            InterviewScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onMatterSaved = { caseId, wasUpdate ->
                    if (wasUpdate) {
                        // Came from that matter's screen; it observes the store and is already up to date.
                        navController.popBackStack()
                    } else {
                        navController.navigate(Destination.CaseFile.createRoute(caseId)) {
                            popUpTo(Destination.Interview.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = Destination.CaseFile.route,
            arguments = listOf(navArgument(CaseFileViewModel.ARG_CASE_ID) { type = NavType.StringType })
        ) {
            val viewModel: CaseFileViewModel = hiltViewModel()
            CaseFileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinueInterview = { caseId ->
                    navController.navigate(Destination.Interview.createRoute(caseId))
                }
            )
        }

        composable(Destination.EditMatter.route) {
            val viewModel: EditMatterViewModel = hiltViewModel()
            EditMatterScreen(
                viewModel = viewModel,
                onSaved = { matterId ->
                    navController.navigate(Destination.MatterDetail.createRoute(matterId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destination.MatterDetail.route,
            arguments = listOf(navArgument("matterId") { type = NavType.LongType })
        ) {
            val viewModel: MatterDetailViewModel = hiltViewModel()
            MatterDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenAnalysis = {
                    val matterId = it.arguments?.getLong("matterId") ?: 0L
                    navController.navigate(Destination.AnalysisResult.createRoute(matterId))
                }
            )
        }

        composable(
            route = Destination.AnalysisResult.route,
            arguments = listOf(navArgument("matterId") { type = NavType.LongType })
        ) {
            val viewModel: AnalysisViewModel = hiltViewModel()
            AnalysisResultScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}