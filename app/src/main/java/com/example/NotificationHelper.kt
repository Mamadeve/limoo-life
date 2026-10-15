package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_GENERAL = "channel_general"
    const val CHANNEL_ORDERS = "channel_orders"
    const val CHANNEL_DISCOUNTS = "channel_discounts"

    const val EXTRA_TARGET_URL = "extra_target_url"

    fun setupNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                context.getString(R.string.channel_general_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_general_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            val ordersChannel = NotificationChannel(
                CHANNEL_ORDERS,
                context.getString(R.string.channel_orders_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_orders_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            val discountsChannel = NotificationChannel(
                CHANNEL_DISCOUNTS,
                context.getString(R.string.channel_discounts_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_discounts_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(generalChannel, ordersChannel, discountsChannel)
            )
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        targetUrl: String? = null,
        channelId: String = CHANNEL_GENERAL,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!targetUrl.isNullOrEmpty()) {
                putExtra(EXTRA_TARGET_URL, targetUrl)
                data = Uri.parse(targetUrl)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.img_limoo_logo)
        } catch (_: Exception) {
            null
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon)
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted yet
        }
    }
}
