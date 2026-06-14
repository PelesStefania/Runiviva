package com.pelesstefania.runiviva.data

import android.content.Context
import com.pelesstefania.runiviva.model.AiNotificationContext
import com.pelesstefania.runiviva.model.AppUser

class AiNotificationRepository(
    private val context: Context
) {

    private val localRunRepository = LocalRunRepository(context)
    private val friendRepository = FriendRepository()

    suspend fun buildContext(
        user: AppUser
    ): AiNotificationContext {

        val runs = localRunRepository.getRunsForUser(user.uid)
        val today = java.time.LocalDate.now().toString()

        val todayRuns = localRunRepository.getRunsForUserByDate(
            user.uid,
            today
        )

        val latestRun = runs.maxByOrNull {
            it.startTime
        }

        val friendIds = friendRepository.getFriends(user.uid)
        val hasFriends = friendIds.isNotEmpty()

        var friendRunsLast7Days = 0

        if (hasFriends) {
            val friendRunRepository = FriendRunRepository()
            for (friendId in friendIds) {
                friendRunsLast7Days += friendRunRepository
                    .getFriendRunsLast7Days(friendId)
                    .size
            }
        }

        return AiNotificationContext(
            username = user.username,
            notificationTone = user.notificationTone,
            ranToday = todayRuns.isNotEmpty(),
            todayDistanceKm = todayRuns.sumOf { it.distanceKm },
            totalDistanceKm = runs.sumOf { it.distanceKm },
            lastRunLatitude = latestRun?.startLatitude,
            lastRunLongitude = latestRun?.startLongitude,
            friendRunsLast7Days = friendRunsLast7Days,
            hasFriends = hasFriends
        )
    }
}