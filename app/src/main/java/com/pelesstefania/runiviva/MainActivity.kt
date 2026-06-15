package com.pelesstefania.runiviva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.navigation.AppNavigation
import com.pelesstefania.runiviva.notifications.NotificationScheduler
import com.pelesstefania.runiviva.ui.theme.RunivivaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notifications if user is logged in
        if (FirebaseAuth.getInstance().currentUser != null) {
            val notificationScheduler = NotificationScheduler(this)
            notificationScheduler.scheduleNotifications(isEnabled = true)
        }

        setContent {
            RunivivaTheme {
                AppNavigation()
            }
        }
    }
}