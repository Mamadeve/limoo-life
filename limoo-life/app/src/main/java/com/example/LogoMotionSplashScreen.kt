package com.example

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LimooAmber
import com.example.ui.theme.LimooGreen
import com.example.ui.theme.LimooNavyDark
import com.example.ui.theme.LimooYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LogoMotionSplashScreen(
    onAnimationComplete: () -> Unit
) {
    val scale = remember { Animatable(0.2f) }
    val rotation = remember { Animatable(-180f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(40f) }
    val glowAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    LaunchedEffect(Unit) {
        // Step 1: Scale and rotate entrance with spring physics
        launch {
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing)
            )
        }
        launch {
            glowAlpha.animateTo(
                targetValue = 0.8f,
                animationSpec = tween(durationMillis = 1100)
            )
        }

        delay(600)
        // Step 2: Text reveal
        launch {
            textAlpha.animateTo(1f, animationSpec = tween(700))
        }
        launch {
            textOffsetY.animateTo(0f, animationSpec = spring(dampingRatio = 0.7f))
        }

        // Display full animation for 2.2 seconds then smoothly transition
        delay(1600)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF162A45),
                        LimooNavyDark,
                        Color(0xFF060D17)
                    )
                )
            )
            .testTag("logo_motion_splash"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Glowing Backdrop Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                // Subtle ambient citrus glow
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(pulseScale)
                        .alpha(glowAlpha.value * 0.45f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    LimooYellow.copy(alpha = 0.5f),
                                    LimooAmber.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Lemon Logo Image with animation
                Image(
                    painter = painterResource(id = R.drawable.img_limoo_logo),
                    contentDescription = "Limoo Life Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scale.value * pulseScale)
                        .rotate(rotation.value)
                        .clip(CircleShape)
                        .testTag("splash_lemon_logo")
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Brand Name & Slogan with slide & fade
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    color = LimooYellow,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.testTag("splash_app_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.app_tagline),
                    color = Color(0xFFBAC7D5),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("splash_tagline")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Subtle animated loading dots
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(spinAngle)
                        .clip(CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .align(Alignment.TopCenter)
                            .clip(CircleShape)
                            .background(LimooGreen)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .align(Alignment.BottomCenter)
                            .clip(CircleShape)
                            .background(LimooYellow)
                    )
                }
            }
        }
    }
}
