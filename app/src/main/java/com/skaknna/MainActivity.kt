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
import com.skaknna.data.local.AppDatabase
import com.skaknna.data.remote.RemoteBoardService
import com.skaknna.data.repository.BoardRepository
import com.skaknna.viewmodel.BoardViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Storage
        val database = AppDatabase.getDatabase(this)
        val remoteService = RemoteBoardService()
        val repository = BoardRepository(database.boardDao(), remoteService)
        val viewModelFactory = BoardViewModelFactory(repository)

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
                    AppNavigation(paddingValues = it, viewModelFactory = viewModelFactory)
                }
            }
        }
    }
}
