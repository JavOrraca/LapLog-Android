package com.javierorraca.laplog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.javierorraca.laplog.ui.LapLogApp
import com.javierorraca.laplog.ui.LapLogViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LapLogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LapLogApp(viewModel = viewModel)
        }
    }
}
