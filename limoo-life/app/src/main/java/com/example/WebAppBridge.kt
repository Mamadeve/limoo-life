package com.example

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.webkit.JavascriptInterface
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebAppBridge(
    private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val onRequestPermission: () -> Unit
) {
    @JavascriptInterface
    fun showNotification(title: String, message: String, url: String? = null) {
        NotificationHelper.showNotification(
            context = context,
            title = title,
            message = message,
            targetUrl = url,
            channelId = NotificationHelper.CHANNEL_GENERAL
        )
    }

    @JavascriptInterface
    fun sendOrderNotification(title: String, message: String, url: String? = null) {
        NotificationHelper.showNotification(
            context = context,
            title = title,
            message = message,
            targetUrl = url,
            channelId = NotificationHelper.CHANNEL_ORDERS
        )
    }

    @JavascriptInterface
    fun sendDiscountNotification(title: String, message: String, url: String? = null) {
        NotificationHelper.showNotification(
            context = context,
            title = title,
            message = message,
            targetUrl = url,
            channelId = NotificationHelper.CHANNEL_DISCOUNTS
        )
    }

    @JavascriptInterface
    fun isOnline(): Boolean {
        return networkMonitor.isOnline.value
    }

    @JavascriptInterface
    fun getAppVersion(): String {
        return "1.0.0"
    }

    @JavascriptInterface
    fun requestNotificationPermission() {
        onRequestPermission()
    }

    @JavascriptInterface
    fun share(title: String, url: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n$url")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(shareIntent)
    }

    @JavascriptInterface
    fun vibrate(durationMs: Long) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs.coerceIn(10, 1000),
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs.coerceIn(10, 1000))
            }
        } catch (_: Exception) {
        }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
