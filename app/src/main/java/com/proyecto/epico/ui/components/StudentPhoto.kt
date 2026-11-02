package com.proyecto.epico.ui.components

import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.proyecto.epico.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Caché en memoria de las fotos ya decodificadas, para no releer el archivo en cada scroll.
 */
object PhotoCache {
    private val cache = object : LruCache<String, Bitmap>(20 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    fun peek(path: String): Bitmap? = cache.get(path)

    fun load(path: String): Bitmap? {
        cache.get(path)?.let { return it }
        val bitmap = ImageUtils.loadPhoto(path, 1000) ?: return null
        cache.put(path, bitmap)
        return bitmap
    }

    fun remove(path: String) {
        cache.remove(path)
    }
}

/**
 * Muestra la foto de un alumno. Mientras carga (o si no existe) muestra un marcador.
 */
@Composable
fun StudentPhoto(
    path: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap by produceState<Bitmap?>(initialValue = PhotoCache.peek(path), key1 = path) {
        if (value == null) {
            value = withContext(Dispatchers.IO) { PhotoCache.load(path) }
        }
    }

    Box(
        modifier = modifier.background(Color(0xFF12324F)),
        contentAlignment = Alignment.Center
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "Foto del alumno",
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(42.dp)
            )
        }
    }
}
