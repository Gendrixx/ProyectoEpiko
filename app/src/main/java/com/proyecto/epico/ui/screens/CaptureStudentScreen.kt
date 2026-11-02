package com.proyecto.epico.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.proyecto.epico.model.StudentGrade
import com.proyecto.epico.ui.components.GradeSelector
import com.proyecto.epico.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val CardColor = Color(0xFF0E1F36)
private val AccentColor = Color(0xFF38BDF8)
private val MutedText = Color(0xFF9FB3C8)
private val BorderColor = Color(0xFF1E3A5F)

/**
 * Pantalla para capturar un alumno nuevo:
 * 1. Tomar una foto o elegir una de la galería.
 * 2. Ponerle nombre, una descripción corta y su grado (AL1, AL2 o AL3).
 * 3. Guardarlo en la colección.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureStudentScreen(
    onCancel: () -> Unit,
    onSave: suspend (name: String, description: String, grade: StudentGrade, photoUri: Uri) -> Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf(StudentGrade.AL1) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Archivo temporal creado para la cámara (se borra si se cancela o se guarda).
    var capturedFile by remember { mutableStateOf<File?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCameraFile
        val uri = pendingCameraUri
        if (success && uri != null && file != null) {
            capturedFile?.delete()
            capturedFile = file
            photoUri = uri
            errorMessage = null
        } else {
            file?.delete()
        }
        pendingCameraFile = null
        pendingCameraUri = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            capturedFile?.delete()
            capturedFile = null
            photoUri = uri
            errorMessage = null
        }
    }

    // Vista previa de la foto elegida
    LaunchedEffect(photoUri) {
        val uri = photoUri
        previewBitmap = if (uri == null) {
            null
        } else {
            withContext(Dispatchers.IO) { ImageUtils.loadPreview(context, uri, 1400) }
        }
    }

    // Si el usuario sale de la pantalla sin guardar, borramos el temporal de la cámara.
    val latestCapturedFile by rememberUpdatedState(capturedFile)
    DisposableEffect(Unit) {
        onDispose { latestCapturedFile?.delete() }
    }

    fun launchCamera() {
        errorMessage = null
        val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = runCatching {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }.getOrNull()

        if (uri == null) {
            errorMessage = "No se pudo preparar la cámara en este dispositivo."
            return
        }

        pendingCameraFile = file
        pendingCameraUri = uri
        runCatching { cameraLauncher.launch(uri) }.onFailure {
            pendingCameraFile?.delete()
            pendingCameraFile = null
            pendingCameraUri = null
            errorMessage = "No se encontró una app de cámara. Prueba eligiendo una foto de la galería."
        }
    }

    val canSave = photoUri != null && name.isNotBlank() && !isSaving

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            CaptureTopBar(onCancel = onCancel)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // ================= FOTO =================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.15f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardColor)
                        .border(
                            width = 2.dp,
                            color = if (photoUri != null) {
                                AccentColor.copy(alpha = 0.65f)
                            } else {
                                Color.White.copy(alpha = 0.12f)
                            },
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = previewBitmap
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto seleccionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        AccentColor.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = AccentColor,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Agrega una foto del alumno",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tómala en el momento o elige una de tu galería",
                                color = MutedText,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { launchCamera() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            contentColor = Color(0xFF052F3F)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(text = "Tomar foto", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(text = "Galería", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ================= NOMBRE =================
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 40) name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre del alumno") },
                    placeholder = { Text("Ej. Ana la imparable") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AccentColor
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    colors = fieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ================= DESCRIPCIÓN =================
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 160) description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción corta") },
                    placeholder = { Text("¿Qué hace especial a este alumno?") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    supportingText = {
                        Text(
                            text = "${description.length}/160",
                            color = MutedText,
                            fontSize = 10.sp
                        )
                    },
                    colors = fieldColors()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ================= GRADO =================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0B2A44))
                        .border(
                            1.dp,
                            AccentColor.copy(alpha = 0.35f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎖️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grado del alumno",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Elige si cursa el primer, segundo o tercer año.",
                        color = MutedText,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    GradeSelector(
                        selected = grade,
                        onSelect = { grade = it }
                    )
                }

                errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message,
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ================= GUARDAR =================
                Button(
                    onClick = {
                        val uri = photoUri ?: return@Button
                        coroutineScope.launch {
                            isSaving = true
                            errorMessage = null
                            val saved = onSave(name.trim(), description.trim(), grade, uri)
                            isSaving = false
                            if (saved) {
                                capturedFile?.delete()
                            } else {
                                errorMessage = "No se pudo guardar la foto. Intenta con otra imagen."
                            }
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = Color(0xFF052F3F),
                        disabledContainerColor = Color(0xFF132A44),
                        disabledContentColor = Color(0xFF6B87A3)
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color(0xFF052F3F),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guardar en mi colección",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }

                if (!canSave && !isSaving) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (photoUri == null) {
                            "Primero agrega una foto para poder guardar."
                        } else {
                            "Escribe el nombre del alumno para poder guardar."
                        },
                        color = MutedText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun CaptureTopBar(onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }
        Column {
            Text(
                text = "CAPTURAR ALUMNO",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "Foto, nombre, descripción y grado",
                color = MutedText,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentColor,
    unfocusedBorderColor = BorderColor,
    focusedContainerColor = CardColor,
    unfocusedContainerColor = CardColor,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = AccentColor,
    unfocusedLabelColor = MutedText,
    cursorColor = AccentColor
)
