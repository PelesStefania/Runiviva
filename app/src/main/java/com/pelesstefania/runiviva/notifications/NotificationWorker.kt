package com.pelesstefania.runiviva.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.data.AiNotificationRepository
import com.pelesstefania.runiviva.data.AiService
import com.pelesstefania.runiviva.data.UserRepository
import com.pelesstefania.runiviva.data.CalendarStatusRepository
import com.pelesstefania.runiviva.data.LocalRunRepository
import java.time.LocalDate

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

            val localRunRepository = LocalRunRepository(applicationContext)
            val calendarStatusRepository = CalendarStatusRepository(applicationContext)

            val user = userRepository.getUserByIdSuspend(currentUser.uid)
                ?: return Result.retry()

            if (!user.notificationsEnabled) {
                return Result.success()
            }

            val today = LocalDate.now().toString()

            val runCountToday = localRunRepository.getRunCountForDate(
                userId = user.uid,
                date = today
            )

            if (
                user.notificationTone == "injury" &&
                runCountToday == 0
            ) {
                calendarStatusRepository.markSickDay(
                    userId = user.uid,
                    date = today
                )
            }


            val aiContext = aiNotificationRepository.buildContext(user)

            val aiMessage = aiService.generateNotification(aiContext)

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