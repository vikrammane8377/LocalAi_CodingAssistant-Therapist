package com.example.programmingailocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.programmingailocal.ui.theme.ProgrammingAiLocalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Chaquopy Python runtime once per process.
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        enableEdgeToEdge()
        setContent {
            ProgrammingAiLocalTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "home") {
                    composable("home") {
                        com.example.programmingailocal.ui.HomeScreen(
                            navigateToChat = { navController.navigate("chat") },
                            navigateToProgramming = { navController.navigate("programming") }
                        )
                    }
                    composable("chat") {
                        com.example.programmingailocal.ui.ChatScreen()
                    }
                    composable("programming") {
                        com.example.programmingailocal.ui.ProgrammingScreen()
                    }
                }
            }
        }
    }
}

// Previous Greeting composables removed; not used anymore.