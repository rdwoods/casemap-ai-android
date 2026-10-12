package com.casemapai.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.casemapai.ui.navigation.CaseMapNavHost
import com.casemapai.ui.theme.AppBackground

@Composable
fun CaseMapApp() {
    Surface(color = AppBackground) {
        CaseMapNavHost()
    }
}
