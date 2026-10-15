package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LimooGreen
import com.example.ui.theme.LimooYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickControlsSheet(
    sheetState: SheetState,
    currentUrl: String,
    isOnline: Boolean,
    webView: WebView?,
    onDismiss: () -> Unit,
    onReload: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("quick_controls_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Online/Offline Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isOnline) LimooGreen.copy(alpha = 0.15f) else Color(0xFFE53935).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) LimooGreen else Color(0xFFE53935))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "آنلاین" else "آفلاین (کش)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOnline) LimooGreen else Color(0xFFE53935)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Options List
            ControlOptionItem(
                icon = Icons.Default.Refresh,
                iconTint = LimooYellow,
                title = stringResource(id = R.string.reload_page),
                subtitle = "تازه‌سازی محتوای صفحه جاری",
                testTag = "opt_reload",
                onClick = {
                    onReload()
                    onDismiss()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            ControlOptionItem(
                icon = Icons.Default.NotificationsActive,
                iconTint = Color(0xFF29B6F6),
                title = "ارسال نمونه اعلان مستقیم",
                subtitle = "تست و بررسی دریافت نوتیفیکیشن روی گوشی",
                testTag = "opt_test_notification",
                onClick = {
                    NotificationHelper.showNotification(
                        context = context,
                        title = "🍋 لیمو لایف - اعلان ویژه",
                        message = "اعلان‌های مستقیم با موفقیت در گوشی شما فعال شد!",
                        targetUrl = currentUrl,
                        channelId = NotificationHelper.CHANNEL_GENERAL
                    )
                    Toast.makeText(context, "اعلان تستی ارسال شد", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            ControlOptionItem(
                icon = Icons.Default.DeleteSweep,
                iconTint = Color(0xFFFFA726),
                title = stringResource(id = R.string.clear_cache),
                subtitle = "پاکسازی فایل‌های موقت، داده‌ها و کوکی‌های وب",
                testTag = "opt_clear_cache",
                onClick = {
                    webView?.clearCache(true)
                    webView?.clearFormData()
                    webView?.clearHistory()
                    WebStorage.getInstance().deleteAllData()
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                    Toast.makeText(
                        context,
                        context.getString(R.string.cache_cleared_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                    onReload()
                    onDismiss()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            ControlOptionItem(
                icon = Icons.Default.Share,
                iconTint = LimooGreen,
                title = stringResource(id = R.string.share_page),
                subtitle = "ارسال پیوند صفحه برای دوستان",
                testTag = "opt_share",
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, currentUrl)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری"))
                    onDismiss()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            ControlOptionItem(
                icon = Icons.Default.OpenInBrowser,
                iconTint = Color(0xFFAB47BC),
                title = stringResource(id = R.string.open_in_browser),
                subtitle = "مشاهده در مرورگر پیش‌فرض سیستم",
                testTag = "opt_browser",
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl)))
                    } catch (_: Exception) {
                    }
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ControlOptionItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
