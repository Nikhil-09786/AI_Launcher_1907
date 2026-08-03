package com.example.ailauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ailauncher.ui.MainScreen
import com.example.ailauncher.ui.MainViewModel
import com.example.ailauncher.ui.theme.AILauncherTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge layout for pitch black full-screen OLED immersion
        enableEdgeToEdge()

        setContent {
            AILauncherTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
