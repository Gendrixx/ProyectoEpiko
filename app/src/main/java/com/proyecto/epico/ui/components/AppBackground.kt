package com.proyecto.epico.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.epico.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Rutas donde se busca la foto de la ESOFA para usarla de fondo.
 * Basta con copiar la imagen a `app/src/main/assets/esofa.jpg` (o .jpeg/.png).
 */
private val BACKGROUND_ASSETS = listOf(
    "esofa.jpg",
    "esofa.jpeg",
    "esofa.png",
    "esofa.webp",
    "fondo/esofa.jpg",
    "fondo/esofa.jpeg",
    "fondo/esofa.png",
    "esofa_fondo.jpg",
    "esofa_fondo.jpeg",
    "esofa_fondo.png"
)

/**
 * Fondo decorativo de la app:
 * - Si existe una foto de la ESOFA en `assets`, la muestra atenuada.
 * - Si no, pinta un degradado azul con la marca ESOFA.
 */
@Composable
fun AppBackground(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var photo by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        photo = withContext(Dispatchers.IO) {
            loadEsosaBackground(context)?.asImageBitmap()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Base: degradado azul profundo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF07182E),
                            Color(0xFF0A2440),
                            Color(0xFF061224)
                        )
                    )
                )
        )

        val image = photo
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = "Escuela de Suboficiales de la FAP",
                contentScale = ContentScale.Crop,
                alpha = 0.45f,
                modifier = Modifier.fillMaxSize()
            )
            // Oscurece la foto para que el contenido se lea bien
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xD907182E),
                                Color(0xEE061224)
                            )
                        )
                    )
            )
        } else {
            // Decoración: resplandores celestes
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x3338BDF8), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.05f),
                        radius = size.width * 0.75f
                    ),
                    radius = size.width * 0.75f,
                    center = Offset(size.width * 0.85f, size.height * 0.05f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x222563EB), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.75f),
                        radius = size.width * 0.9f
                    ),
                    radius = size.width * 0.9f,
                    center = Offset(size.width * 0.1f, size.height * 0.75f)
                )
            }

            // Marca de agua ESOFA
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8).copy(alpha = 0.10f),
                    modifier = Modifier.size(130.dp)
                )
                Text(
                    text = "ESOFA",
                    color = Color(0xFF38BDF8).copy(alpha = 0.09f),
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp,
                    letterSpacing = 10.sp
                )
                Text(
                    text = "ESCUELA DE SUBOFICIALES DE LA FAP",
                    color = Color(0xFF38BDF8).copy(alpha = 0.07f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

private fun loadEsosaBackground(context: Context): Bitmap? {
    val assetPath = BACKGROUND_ASSETS.firstOrNull { path ->
        runCatching { context.assets.open(path).close() }.isSuccess
    } ?: return null

    return ImageUtils.loadAssetBitmap(context, assetPath, 1600)
}
