package com.proyecto.epico.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

/**
 * Utilidades para leer, rotar y guardar las fotos de los alumnos.
 *
 * Todo se hace con APIs del sistema (sin librerías externas):
 * - Se decodifica con muestreo para no llenar la memoria con fotos gigantes.
 * - Se corrige la orientación EXIF de las fotos de cámara.
 * - Se normaliza la foto a un tamaño razonable y se guarda en el almacenamiento interno.
 */
object ImageUtils {

    private const val MAX_SAVED_SIZE = 1600
    private const val JPEG_QUALITY = 88

    /**
     * Copia la imagen indicada por [source] al archivo [destination], corrigiendo la orientación
     * y reduciendo su tamaño. Devuelve la ruta final o null si no se pudo procesar.
     */
    fun saveNormalizedPhoto(context: Context, source: Uri, destination: File): String? {
        return runCatching {
            val input = context.contentResolver.openInputStream(source) ?: return null
            saveNormalizedStream(context.cacheDir, input, destination)
        }.getOrNull()
    }

    /**
     * Guarda una foto incluida en `assets` (por ejemplo las de ejemplo) en el almacenamiento interno.
     */
    fun saveNormalizedAsset(context: Context, assetPath: String, destination: File): String? {
        return runCatching {
            val input = context.assets.open(assetPath)
            saveNormalizedStream(context.cacheDir, input, destination)
        }.getOrNull()
    }

    /**
     * Carga una vista previa (reducida) de una imagen seleccionada por el usuario.
     */
    fun loadPreview(context: Context, uri: Uri, maxSize: Int): Bitmap? {
        return runCatching {
            val temp = File.createTempFile("preview_", ".jpg", context.cacheDir)
            try {
                val input = context.contentResolver.openInputStream(uri) ?: return null
                input.use { stream ->
                    temp.outputStream().use { output -> stream.copyTo(output) }
                }
                loadPhoto(temp.absolutePath, maxSize)
            } finally {
                temp.delete()
            }
        }.getOrNull()
    }

    /**
     * Carga un bitmap desde la carpeta `assets`.
     */
    fun loadAssetBitmap(context: Context, assetPath: String, maxSize: Int): Bitmap? {
        return runCatching {
            val temp = File.createTempFile("asset_", ".jpg", context.cacheDir)
            try {
                context.assets.open(assetPath).use { stream ->
                    temp.outputStream().use { output -> stream.copyTo(output) }
                }
                loadPhoto(temp.absolutePath, maxSize)
            } finally {
                temp.delete()
            }
        }.getOrNull()
    }

    /**
     * Decodifica un archivo de imagen aplicando la rotación EXIF correspondiente.
     */
    fun loadPhoto(path: String, maxSize: Int): Bitmap? {
        val decoded = decodeSampled(path, maxSize) ?: return null
        val rotation = readRotationDegrees(path)
        if (rotation == 0f) return decoded

        return runCatching {
            Bitmap.createBitmap(
                decoded,
                0,
                0,
                decoded.width,
                decoded.height,
                Matrix().apply { postRotate(rotation) },
                true
            ).also { rotated ->
                if (rotated !== decoded) decoded.recycle()
            }
        }.getOrDefault(decoded)
    }

    private fun saveNormalizedStream(cacheDir: File, input: InputStream, destination: File): String? {
        val temp = File.createTempFile("normalize_", ".jpg", cacheDir)
        return try {
            input.use { stream ->
                temp.outputStream().use { output -> stream.copyTo(output) }
            }

            val bitmap = loadPhoto(temp.absolutePath, MAX_SAVED_SIZE) ?: return null
            destination.parentFile?.mkdirs()
            FileOutputStream(destination).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            bitmap.recycle()
            destination.absolutePath
        } finally {
            temp.delete()
        }
    }

    private fun decodeSampled(path: String, maxSize: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, maxSize)
        }
        return BitmapFactory.decodeFile(path, options)
    }

    private fun calculateSampleSize(width: Int, height: Int, maxSize: Int): Int {
        var sample = 1
        val longest = max(width, height)
        while (longest / (sample * 2) >= maxSize) {
            sample *= 2
        }
        return sample
    }

    private fun readRotationDegrees(path: String): Float = runCatching {
        when (
            ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }.getOrDefault(0f)
}
