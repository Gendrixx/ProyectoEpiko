package com.proyecto.epico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.proyecto.epico.data.StudentRepository
import com.proyecto.epico.model.CapturedStudent
import com.proyecto.epico.ui.components.AppBackground
import com.proyecto.epico.ui.components.CaptureCelebrationOverlay
import com.proyecto.epico.ui.components.EditStudentDialog
import com.proyecto.epico.ui.components.PhotoCache
import com.proyecto.epico.ui.components.StudentDetailDialog
import com.proyecto.epico.ui.screens.CaptureStudentScreen
import com.proyecto.epico.ui.screens.StudentCollectionScreen
import com.proyecto.epico.ui.theme.ProyectoepicoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProyectoepicoTheme {
                StudentCollectorApp()
            }
        }
    }
}

private enum class AppScreen { COLLECTION, CAPTURE }

/**
 * Colector de alumnos: pantalla de colección + pantalla de captura.
 */
@Composable
fun StudentCollectorApp() {
    val context = LocalContext.current
    val repository = remember { StudentRepository(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()

    val students = remember { mutableStateListOf<CapturedStudent>() }
    var screen by remember { mutableStateOf(AppScreen.COLLECTION) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedStudent by remember { mutableStateOf<CapturedStudent?>(null) }
    var editingStudent by remember { mutableStateOf<CapturedStudent?>(null) }
    var celebrationStudent by remember { mutableStateOf<CapturedStudent?>(null) }

    // Cargar la colección guardada (y las fotos de ejemplo la primera vez)
    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) {
            repository.seedExamplesIfNeeded()
            repository.load()
        }
        students.clear()
        students.addAll(loaded)
        isLoading = false
    }

    BackHandler(enabled = screen == AppScreen.CAPTURE) {
        screen = AppScreen.COLLECTION
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppBackground()

        when {
            isLoading -> CircularProgressIndicator(
                color = Color(0xFF38BDF8),
                modifier = Modifier.align(Alignment.Center)
            )

            screen == AppScreen.COLLECTION -> StudentCollectionScreen(
                students = students,
                onCaptureClick = { screen = AppScreen.CAPTURE },
                onStudentClick = { student -> selectedStudent = student }
            )

            else -> CaptureStudentScreen(
                onCancel = { screen = AppScreen.COLLECTION },
                onSave = { name, description, grade, photoUri ->
                    val saved = withContext(Dispatchers.IO) {
                        repository.create(name, description, grade, photoUri)
                    }
                    if (saved != null) {
                        students.add(0, saved)
                        celebrationStudent = saved
                        screen = AppScreen.COLLECTION
                        true
                    } else {
                        false
                    }
                }
            )
        }

        // ================= DETALLE DE UN ALUMNO =================
        selectedStudent?.let { student ->
            StudentDetailDialog(
                student = student,
                onDismiss = { selectedStudent = null },
                onEdit = {
                    selectedStudent = null
                    editingStudent = student
                },
                onDelete = {
                    selectedStudent = null
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) { repository.delete(student) }
                        PhotoCache.remove(student.photoPath)
                        students.remove(student)
                    }
                }
            )
        }

        // ================= EDITAR NOMBRE / DESCRIPCIÓN / GRADO =================
        editingStudent?.let { student ->
            EditStudentDialog(
                student = student,
                onDismiss = { editingStudent = null },
                onSave = { name, description, grade ->
                    coroutineScope.launch {
                        val updated = withContext(Dispatchers.IO) {
                            repository.update(student, name, description, grade)
                        }
                        val index = students.indexOfFirst { it.id == updated.id }
                        if (index >= 0) students[index] = updated
                        editingStudent = null
                    }
                }
            )
        }

        // ================= CELEBRACIÓN "¡CAPTURADO!" =================
        celebrationStudent?.let { student ->
            CaptureCelebrationOverlay(
                student = student,
                onDismiss = { celebrationStudent = null }
            )
        }
    }
}
