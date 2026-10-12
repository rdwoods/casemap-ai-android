package com.casemapai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.casemapai.app.CaseMapApp
import com.casemapai.ui.theme.CaseMapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CaseMapTheme {
                CaseMapApp()
            }
        }
    }
}