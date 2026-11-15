package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Smoothly animate progress bar to 100% over 2.2 seconds
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
        )
        delay(200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onFinished() } // Allow tapping to skip
    ) {
        // Fullscreen Maya Splash Art
        Image(
            painter = painterResource(id = R.drawable.maya_splash_art),
            contentDescription = "Maya AI Loading Screen",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom Overlay with Title, Subtitle and Animated Gradient Progress Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // M A Y A Title
            Text(
                text = "M A Y A",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Ek second... load ho raha hai 💗",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // Glowing Neon Gradient Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF38BDF8), // Cyan
                                    Color(0xFF60A5FA), // Blue
                                    Color(0xFFEC4899), // Pink
                                    Color(0xFFF43F5E)  // Rose
                                )
                            )
                        )
                )
            }
        }
    }
}
