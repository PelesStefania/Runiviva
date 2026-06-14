package com.pelesstefania.runiviva.data

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.pelesstefania.runiviva.model.AiMessage
import com.pelesstefania.runiviva.model.AiNotificationContext

class AiService {

    private val model = Firebase.ai(
        backend = GenerativeBackend.googleAI()
    ).generativeModel(
        modelName = "gemini-2.5-flash"
    )

    suspend fun generateNotification(
        context: AiNotificationContext
    ): AiMessage {

        val toneInstructions = when(context.notificationTone) {
            "funny" -> """
                Write like a funny, casual friend texting another friend.
                Use light sarcasm and playful humor. Never sound like a formal fitness coach.
                
                Examples of the exact vibe needed:
                - Your running shoes are starting to think you've abandoned them.
                - The couch called. It said you're spending too much time together.
                - Even a 10-minute run counts. Nice try though.
                - Future you is begging for a short run today.
            """.trimIndent()

            "encouraging" -> """
                Write like a genuinely supportive, empathetic friend.
                Be warm, kind, and grounding. Never sound like an overly intense motivational speaker.
                
                Examples of the exact vibe needed:
                - You've already proven you can do hard things.
                - A short run is still progress.
                - Be proud of every step you take today.
                - Consistency matters more than perfection.
            """.trimIndent()

            "competitive" -> """
                Write like an ambitious friend challenging the user to push themselves.
                Be driving and challenging, but keep it friendly and never toxic.
                
                Examples of the exact vibe needed:
                - You've got more in you than yesterday. Prove it.
                - Someone else is training today. Are you?
                - Time to beat your previous run.
                - Let's see if today's version of you is stronger.
            """.trimIndent()

            "rude" -> """
                Write like a brutally honest, no-nonsense friend who uses tough love.
                Use teasing and friendly trash-talk. Never insult appearance, intelligence, or worth.
                
                Examples of the exact vibe needed:
                - Your shoes deserve better treatment.
                - The hardest part is apparently standing up.
                - Your excuses are getting more exercise than you are.
                - You know exactly what you should be doing.
            """.trimIndent()

            else -> "Write a casual text message encouraging the user to stay active."
        }

        val userContext = buildString {
            appendLine("- Ran today: ${if (context.ranToday) "Yes (${context.todayDistanceKm} km)" else "No"}")
            appendLine("- Total career distance: ${context.totalDistanceKm} km")

            if (context.hasFriends) {
                appendLine("- Total runs by friends this week: ${context.friendRunsLast7Days}")
            } else {
                appendLine("- User has no friends added in the app.")
            }
        }

        val prompt = """
            You are generating a short casual push notification text for a running app called Runiviva.
            
            TONE SPECIFICATION:
            $toneInstructions
            
            USER DATA CONTEXT (Use these details smoothly ONLY if they fit the tone logically):
            $userContext
            
            STRICT RULES:
            1. Maximum 20 words.
            2. Never use the user's name or username.
            3. Do NOT use greetings (e.g., no "Hey", "Hi", "Hello").
            4. Never use templates like "You ran X km today, great job!". Blend context dynamically.
            5. Avoid generic, cliché fitness phrases.
            6. Return ONLY the notification text. No quotes, no markdown, no comments.
            7. If the user has no friends, never mention friends, competition, comparison with others, or social features.
            8. If the user has friends, you MAY use friend activity naturally, especially for the competitive tone.
            9. If the user has friends, friend comparisons should be used mainly for the competitive tone and only occasionally for other tones.
        """.trimIndent()

        val response = model.generateContent(prompt)

        return AiMessage(
            message = response.text?.trim()?.removeSurrounding("\"") ?: "How about a short run today?"
        )
    }
}