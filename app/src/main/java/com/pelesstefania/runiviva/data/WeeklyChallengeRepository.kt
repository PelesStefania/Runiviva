package com.pelesstefania.runiviva.data

import com.pelesstefania.runiviva.model.WeeklyChallengeEntry
import com.pelesstefania.runiviva.model.WeeklyResults
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class WeeklyChallengeRepository {

    private val friendRepository = FriendRepository()
    private val userRepository = UserRepository()
    private val runRepository = RunRepository()

    suspend fun getWeeklyChallenge(
        currentUserId: String
    ): List<WeeklyChallengeEntry> {

        val startOfWeek = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .toString()

        val currentUser = userRepository.getUserByIdSuspend(currentUserId)

        val friendUsers = friendRepository.getFriendUsers(currentUserId)

        val allUsers = buildList {
            if (currentUser != null) add(currentUser)
            addAll(friendUsers)
        }

        val entries = allUsers.map { user ->

            val runsThisWeek = runRepository
                .getRunsForUserSuspend(user.uid)
                .filter {
                    it.date >= startOfWeek
                }

            WeeklyChallengeEntry(
                userId = user.uid,
                username = user.username,
                distanceKm = runsThisWeek.sumOf { it.distanceKm },
                durationSeconds = runsThisWeek.sumOf { it.durationSeconds }
            )
        }

        return entries.sortedWith(
            compareByDescending<WeeklyChallengeEntry> {
                it.distanceKm
            }.thenBy {
                it.durationSeconds
            }
        )
    }

    suspend fun getPreviousWeekResults(
        currentUserId: String
    ): WeeklyResults {

        val startOfCurrentWeek = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val startOfPreviousWeek = startOfCurrentWeek.minusWeeks(1)
        val endOfPreviousWeek = startOfCurrentWeek.minusDays(1)

        val currentUser = userRepository.getUserByIdSuspend(currentUserId)

        val friendUsers = friendRepository.getFriendUsers(currentUserId)

        val allUsers = buildList {
            if (currentUser != null) add(currentUser)
            addAll(friendUsers)
        }

        val entries = allUsers.map { user ->

            val runsLastWeek = runRepository
                .getRunsForUserSuspend(user.uid)
                .filter {

                    val runDate = LocalDate.parse(it.date)

                    !runDate.isBefore(startOfPreviousWeek) &&
                            !runDate.isAfter(endOfPreviousWeek)
                }

            WeeklyChallengeEntry(
                userId = user.uid,
                username = user.username,
                distanceKm = runsLastWeek.sumOf { it.distanceKm },
                durationSeconds = runsLastWeek.sumOf { it.durationSeconds }
            )
        }.sortedWith(
            compareByDescending<WeeklyChallengeEntry> {
                it.distanceKm
            }.thenBy {
                it.durationSeconds
            }
        )

        val userRank = entries.indexOfFirst {
            it.userId == currentUserId
        } + 1

        val userDistance = entries.firstOrNull {
            it.userId == currentUserId
        }?.distanceKm ?: 0.0

        return WeeklyResults(
            top3 = entries.take(3),
            userRank = userRank,
            userDistanceKm = userDistance
        )
    }
}