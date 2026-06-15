package com.greenrou.rovibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.greenrou.rovibe.ui.navigation.RovibeNavHost
import com.greenrou.rovibe.ui.theme.RovibeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RovibeTheme {
                RovibeNavHost()
            }
        }
    }
}
