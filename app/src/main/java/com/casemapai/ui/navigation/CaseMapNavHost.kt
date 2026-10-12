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
                onCreateMatter = { navController.navigate(Destination.EditMatter.route) },
                onStartInterview = { navController.navigate(Destination.Interview.route) },
                onOpenMatter = { matterId ->
                    navController.navigate(Destination.MatterDetail.createRoute(matterId))
                }
            )
        }

        composable(Destination.Interview.route) {
            val viewModel: InterviewViewModel = hiltViewModel()
            InterviewScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
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