package com.pelesstefania.runiviva

import com.pelesstefania.runiviva.model.AppUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUserTest {

    @Test
    fun defaultUserHasNotificationsEnabled() {
        val user = AppUser(
            uid = "user1",
            username = "Antonia",
            usernameLowercase = "antonia",
            email = "test@test.com"
        )

        assertTrue(user.notificationsEnabled)
        assertEquals("funny", user.notificationTone)
        assertEquals(8, user.morningNotificationHour)
        assertEquals(20, user.eveningNotificationHour)
    }
}