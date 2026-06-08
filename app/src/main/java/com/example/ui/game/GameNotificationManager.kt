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
    private const val CHANNEL_NAME = "Royal Crush Play Reminders"
    private const val CHANNEL_DESC = "Fun daily challenges and royal play alerts"
    private const val NOTIFICATION_ID = 5022

    private fun getAttributionContext(context: Context): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.createAttributionContext("notifications")
        } else {
            context
        }
    }

    fun createNotificationChannel(context: Context) {
        val attrContext = getAttributionContext(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                attrContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerPlayReminderNotification(context: Context) {
        val attrContext = getAttributionContext(context)
        val intent = Intent(attrContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            attrContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val messages = listOf(
            "Your Royal Crush lives are fully refilled! Match now!",
            "The Titan is active! Defeat him to secure 500 gold!",
            "Keep your match-3 winning streak alive! Play your level now!",
            "Solve today's puzzle! Tap here to match tiles!"
        )
        val selectedMessage = messages.random()

        val builder = NotificationCompat.Builder(attrContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.star_on) // star asset ensures high rendering compatibility
            .setContentTitle("Royal Crush")
            .setContentText(selectedMessage)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(attrContext)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
