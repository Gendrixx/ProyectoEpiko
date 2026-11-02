package com.proyecto.epico.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.epico.model.CapturedStudent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val spin: Float,
    val circle: Boolean
)

/**
 * Celebración estilo "¡capturado!" con destellos, confeti y un pequeño sonido.
 */
@Composable
fun CaptureCelebrationOverlay(
    student: CapturedStudent,
    onDismiss: () -> Unit
) {
    val grade = student.grade
    BackHandler(enabled = true, onBack = onDismiss)
    val particles = remember {
        val random = Random(7)
        val colors = listOf(
            Color(0xFF38BDF8),
            Color(0xFF60A5FA),
            Color(0xFF93C5FD),
            Color.White,
            Color(0xFF22D3EE),
            Color(0xFF2563EB)
        )
        List(80) {
            ConfettiParticle(
                angle = random.nextFloat() * 2f * PI.toFloat(),
                speed = 0.35f + random.nextFloat() * 0.85f,
                size = 5f + random.nextFloat() * 9f,
                color = colors[random.nextInt(colors.size)],
                rotation = random.nextFloat() * 360f,
                spin = (random.nextFloat() - 0.5f) * 720f,
                circle = random.nextBoolean()
            )
        }
    }

    val burst = remember { Animatable(0f) }
    val flash = remember { Animatable(0.95f) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(student.id) {
        visible = true
        launch { launchTone() }
        launch { flash.animateTo(0f, tween(durationMillis = 650)) }
        burst.animateTo(1f, tween(durationMillis = 1700, easing = LinearEasing))
    }

    val cardScale = animateFloatAsState(
        targetValue = if (visible) 1f else 0.45f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "celebrationScale"
    )
    val cardAlpha = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "celebrationAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04101F).copy(alpha = 0.94f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // ================= CONFETI =================
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = burst.value
            if (t <= 0f || t >= 1f) return@Canvas

            val center = Offset(size.width / 2f, size.height * 0.40f)
            val base = min(size.width, size.height)

            particles.forEach { particle ->
                val distance = particle.speed * base * 0.95f * t
                val x = center.x + cos(particle.angle) * distance
                val y = center.y + sin(particle.angle) * distance * 0.85f + base * 0.45f * t * t
                val alpha = (1f - t).coerceIn(0f, 1f)
                val color = particle.color.copy(alpha = alpha)

                rotate(degrees = particle.rotation + particle.spin * t, pivot = Offset(x, y)) {
                    if (particle.circle) {
                        drawCircle(
                            color = color,
                            radius = particle.size / 2f,
                            center = Offset(x, y)
                        )
                    } else {
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x - particle.size / 2f, y - particle.size / 4f),
                            size = Size(particle.size, particle.size / 2f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
            }
        }

        // ================= TARJETA =================
        Column(
            modifier = Modifier
                .padding(horizontal = 26.dp)
                .graphicsLayer {
                    scaleX = cardScale.value
                    scaleY = cardScale.value
                    alpha = cardAlpha.value
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡ALUMNO CAPTURADO!",
                color = Color(0xFF38BDF8),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            listOf(grade.primaryColor, grade.secondaryColor, Color.White)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
            ) {
                StudentPhoto(
                    path = student.photoPath,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.6f to Color.Transparent,
                                1f to Color(0xFF04101F).copy(alpha = 0.9f)
                            )
                        )
                )
                GradeBadge(
                    grade = grade,
                    showTitle = true,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = student.name,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${grade.code} · ${grade.title}",
                color = grade.primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38BDF8),
                    contentColor = Color(0xFF052F3F)
                )
            ) {
                Text(
                    text = "Ver en mi colección",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Toca en cualquier lado para continuar",
                color = Color(0xFF9FB3C8),
                fontSize = 10.sp
            )
        }

        // ================= DESTELLO DE CÁMARA =================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = flash.value }
                .background(Color.White)
        )
    }
}

/**
 * Pequeño sonido de celebración con el sintetizador del sistema (sin archivos de audio).
 */
private suspend fun launchTone() {
    runCatching {
        val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
        try {
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 120)
            delay(150)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
            delay(260)
        } finally {
            tone.release()
        }
    }
}
