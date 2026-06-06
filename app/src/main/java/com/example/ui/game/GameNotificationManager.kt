package com.example.ui.game

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object GameNotificationManager {
    private const val CHANNEL_ID = "candy_kingdom_reminders"
    private const val CHANNEL_NAME = "Candy Kingdom Play Reminders"
    private const val CHANNEL_DESC = "Fun daily challenges and sweet play alerts"
    private const val NOTIFICATION_ID = 5022

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerPlayReminderNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val messages = listOf(
            "🍭 Your Candy Kingdom lives are fully refilled! Match now! 🔥",
            "🍇 Chocolate titan is active! Defeat him to secure 🪙 500 gold!",
            "🧁 Don't keep the sweet candy waiting! Play level now!",
            "🍪 Solve today's sugar puzzle! Tap here to match candies!"
        )
        val selectedMessage = messages.random()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.star_on) // star asset ensures high rendering compatibility
            .setContentTitle("👑 Candy Kingdom")
            .setContentText(selectedMessage)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
