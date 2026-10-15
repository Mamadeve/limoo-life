package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.theme.LimooNavyDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var networkMonitor: NetworkMonitor
    private var targetUrlFromIntent by mutableStateOf<String?>(null)

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Send an initial welcome notification to demonstrate direct notification feature
                NotificationHelper.showNotification(
                    context = this,
                    title = "خوش‌آمدید به لیمو لایف 🍋",
                    message = "اعلان‌های مستقیم در گوشی شما فعال شد. از تخفیف‌ها و پیشنهادهای شگفت‌انگیز باخبر شوید!",
                    targetUrl = TARGET_URL,
                    channelId = NotificationHelper.CHANNEL_GENERAL
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        networkMonitor = NetworkMonitor(this)
        NotificationHelper.setupNotificationChannels(this)

        handleIntent(intent)

        // Request POST_NOTIFICATIONS permission for Android 13+
        requestNotificationPermission()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LimooNavyDark
                ) {
                    var showSplashScreen by remember { mutableStateOf(true) }

                    Crossfade(
                        targetState = showSplashScreen,
                        animationSpec = tween(durationMillis = 600),
                        label = "splash_crossfade"
                    ) { isSplash ->
                        if (isSplash) {
                            LogoMotionSplashScreen(
                                onAnimationComplete = {
                                    showSplashScreen = false
                                }
                            )
                        } else {
                            WebViewContainer(
                                networkMonitor = networkMonitor,
                                onRequestNotificationPermission = {
                                    requestNotificationPermission()
                                },
                                targetUrlOverride = targetUrlFromIntent,
                                onExitApp = {
                                    finish()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val targetUrl = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_URL)
            ?: intent?.dataString
        if (!targetUrl.isNullOrEmpty()) {
            targetUrlFromIntent = targetUrl
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkMonitor.unregister()
    }
}
