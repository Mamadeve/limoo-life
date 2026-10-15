package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.LimooGreen
import com.example.ui.theme.LimooNavyDark
import com.example.ui.theme.LimooYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val TARGET_URL = "https://limoolife.com"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    networkMonitor: NetworkMonitor,
    onRequestNotificationPermission: () -> Unit,
    targetUrlOverride: String? = null,
    onExitApp: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOnline by networkMonitor.isOnline.collectAsState()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(targetUrlOverride ?: TARGET_URL) }
    var pageTitle by remember { mutableStateOf("لیمو لایف") }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var hasFatalError by remember { mutableStateOf(false) }
    var showQuickControls by remember { mutableStateOf(false) }
    var showRestoredBanner by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    // Handle back button: go back in WebView history if possible
    BackHandler {
        val webView = webViewInstance
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            onExitApp()
        }
    }

    // Monitor transition from offline to online
    LaunchedEffect(isOnline) {
        val webView = webViewInstance ?: return@LaunchedEffect
        val settings = webView.settings
        if (isOnline) {
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            if (hasFatalError) {
                hasFatalError = false
                webView.reload()
            }
            showRestoredBanner = true
            delay(3000)
            showRestoredBanner = false
        } else {
            // Prioritize cached content when offline
            settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
    }

    // React to external URL navigation (e.g. from notification)
    LaunchedEffect(targetUrlOverride) {
        if (!targetUrlOverride.isNullOrEmpty()) {
            webViewInstance?.loadUrl(targetUrlOverride)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("webview_container")
    ) {
        if (hasFatalError && !isOnline) {
            // Friendly Offline Screen
            OfflineFallbackView(
                onRetry = {
                    hasFatalError = false
                    webViewInstance?.reload()
                },
                onGoHome = {
                    hasFatalError = false
                    webViewInstance?.loadUrl(TARGET_URL)
                }
            )
        } else {
            // Main Web App View
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Performance & Hardware Acceleration
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)

                        // WebSettings for modern PWA / Web App
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = true
                            allowContentAccess = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                            // Offline-first caching setup
                            cacheMode = if (isOnline) {
                                WebSettings.LOAD_DEFAULT
                            } else {
                                WebSettings.LOAD_CACHE_ELSE_NETWORK
                            }
                        }

                        // Cookies persistence
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        // JavaScript Native Bridge
                        val bridge = WebAppBridge(
                            context = ctx,
                            networkMonitor = networkMonitor,
                            onRequestPermission = onRequestNotificationPermission
                        )
                        addJavascriptInterface(bridge, "AndroidBridge")
                        addJavascriptInterface(bridge, "LimooNotification")

                        // WebChromeClient
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadProgress = newProgress / 100f
                                isLoading = newProgress < 100
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                if (!title.isNullOrEmpty()) {
                                    pageTitle = title
                                }
                            }
                        }

                        // WebViewClient
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false
                                val uri = Uri.parse(url)
                                val scheme = uri.scheme?.lowercase() ?: ""

                                // Handle intent, tel, mailto, sms, and external messaging links
                                if (scheme == "tel" || scheme == "mailto" || scheme == "sms" ||
                                    scheme == "whatsapp" || scheme == "tg" || scheme == "intent"
                                ) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        ctx.startActivity(intent)
                                        return true
                                    } catch (_: Exception) {
                                        return true
                                    }
                                }

                                // Keep all limoolife pages inside the web view
                                if (uri.host?.contains("limoolife.com", ignoreCase = true) == true) {
                                    return false
                                }

                                // External payment gateways (e.g. Shaparak, banks, Zarinpal) - allow in WebView
                                if (uri.host?.contains("shaparak.ir", ignoreCase = true) == true ||
                                    uri.host?.contains("zarinpal.com", ignoreCase = true) == true
                                ) {
                                    return false
                                }

                                // Other external links: open in external browser
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    ctx.startActivity(intent)
                                    return true
                                } catch (_: Exception) {
                                    return false
                                }
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                url?.let { currentUrl = it }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                url?.let { currentUrl = it }
                                CookieManager.getInstance().flush()
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    // If offline and cache missing, display fallback view
                                    if (!isOnline) {
                                        hasFatalError = true
                                    }
                                }
                            }
                        }

                        // Load target website
                        loadUrl(currentUrl)
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    webViewInstance = webView
                }
            )
        }

        // Top Loading Progress Indicator (Thin & Sleek)
        if (isLoading && loadProgress < 1f) {
            LinearProgressIndicator(
                progress = { loadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .statusBarsPadding()
                    .align(Alignment.TopCenter)
                    .testTag("web_loading_bar"),
                color = LimooYellow,
                trackColor = Color.Transparent
            )
        }

        // Animated Offline Status Banner (Shown when connection is lost)
        AnimatedVisibility(
            visible = !isOnline,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF263238),
                tonalElevation = 6.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = LimooYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "حالت آفلاین (محتوای کش)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LimooYellow.copy(alpha = 0.2f))
                            .clickable {
                                webViewInstance?.reload()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "تلاش مجدد",
                            color = LimooYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Animated Online Restored Banner
        AnimatedVisibility(
            visible = showRestoredBanner,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = LimooGreen,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.img_limoo_logo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "اتصال مجدداً برقرار شد",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Floating Quick Menu Button (Bottom Right)
        FloatingActionButton(
            onClick = { showQuickControls = true },
            containerColor = LimooNavyDark,
            contentColor = LimooYellow,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
                .size(48.dp)
                .testTag("fab_quick_controls")
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Quick Controls",
                modifier = Modifier.size(22.dp)
            )
        }

        // Quick Controls Bottom Sheet
        if (showQuickControls) {
            QuickControlsSheet(
                sheetState = sheetState,
                currentUrl = currentUrl,
                isOnline = isOnline,
                webView = webViewInstance,
                onDismiss = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        showQuickControls = false
                    }
                },
                onReload = {
                    webViewInstance?.reload()
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
        }
    }
}
