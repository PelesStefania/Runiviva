package com.pelesstefania.runiviva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import com.pelesstefania.runiviva.navigation.AppNavigation
import com.pelesstefania.runiviva.ui.theme.RunivivaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RunivivaTheme {
                AppNavigation()
            }
        }
    }
}