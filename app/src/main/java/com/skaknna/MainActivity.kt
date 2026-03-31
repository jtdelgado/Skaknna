package com.skaknna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.skaknna.ui.navigation.AppNavigation
import com.skaknna.ui.theme.SkaknnaTheme
import androidx.compose.ui.graphics.Color
import com.skaknna.ui.components.checkerboardBackground

import androidx.activity.SystemBarStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            SkaknnaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize().checkerboardBackground(),
                    containerColor = Color.Transparent
                ) {
                    AppNavigation(paddingValues = it)
                }
            }
        }
    }
}
