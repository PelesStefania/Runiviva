package com.pelesstefania.runiviva.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    fun scheduleNotifications(isEnabled: Boolean) {
        val workManager = WorkManager.getInstance(context)

        if (!isEnabled) {
            cancelNotifications()
            return
        }

        scheduleDailyNotificationAtTime(
            workManager,
            "morning_notification",
            9
        )

        scheduleDailyNotificationAtTime(
            workManager,
            "evening_notification",
            18
        )
    }

    private fun scheduleDailyNotificationAtTime(
        workManager: WorkManager,
        workTag: String,
        targetHour: Int
    ) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance()

        target.set(Calendar.HOUR_OF_DAY, targetHour)
        target.set(Calendar.MINUTE, 0)
        target.set(Calendar.SECOND, 0)

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delayInMinutes = ((target.timeInMillis - now.timeInMillis) / 1000 / 60).coerceAtLeast(15)

        val notificationRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            24,
            TimeUnit.HOURS
        )
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .addTag(workTag)
            .build()

        workManager.enqueueUniquePeriodicWork(
            workTag,
            ExistingPeriodicWorkPolicy.REPLACE,
            notificationRequest
        )
    }

    fun cancelNotifications() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("morning_notification")
        workManager.cancelAllWorkByTag("evening_notification")
    }
}