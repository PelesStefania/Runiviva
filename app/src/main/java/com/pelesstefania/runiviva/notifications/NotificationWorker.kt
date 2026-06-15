package com.pelesstefania.runiviva.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.data.AiNotificationRepository
import com.pelesstefania.runiviva.data.AiService
import com.pelesstefania.runiviva.data.UserRepository

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return Result.retry()

            val userRepository = UserRepository()
            val aiService = AiService()
            val aiNotificationRepository = AiNotificationRepository(applicationContext)

            // Fetch current user
            val user = userRepository.getUserByIdSuspend(currentUser.uid)
                ?: return Result.retry()

            // Check if notifications are enabled
            if (!user.notificationsEnabled) {
                return Result.success()
            }

            // Build AI context
            val aiContext = aiNotificationRepository.buildContext(user)

            // Generate notification message
            val aiMessage = aiService.generateNotification(aiContext)

            // Send notification
            val notificationManager = NotificationManagerHelper(applicationContext)
            notificationManager.sendNotification(
                title = "Runiviva",
                message = aiMessage.message
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}