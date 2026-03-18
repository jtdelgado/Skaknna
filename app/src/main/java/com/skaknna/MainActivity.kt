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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkaknnaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(paddingValues = it)
                }
            }
        }
    }
}
