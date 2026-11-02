package com.proyecto.epico.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.epico.model.CapturedStudent
import com.proyecto.epico.model.StudentGrade
import com.proyecto.epico.ui.components.StudentCollectionCard

private val CardColor = Color(0xFF0E1F36)
private val AccentColor = Color(0xFF38BDF8)
private val SecondaryColor = Color(0xFF2563EB)
private val MutedText = Color(0xFF9FB3C8)
private val BorderColor = Color(0xFF1E3A5F)

/**
 * Pantalla principal: la colección de alumnos capturados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCollectionScreen(
    students: List<CapturedStudent>,
    onCaptureClick: () -> Unit,
    onStudentClick: (CapturedStudent) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var gradeFilter by remember { mutableStateOf<StudentGrade?>(null) }

    // Se calcula en cada recomposición para que los cambios al editar se reflejen al instante.
    val filteredStudents = students.filter { student ->
        val matchesGrade = gradeFilter == null || student.grade == gradeFilter
        val matchesQuery = searchQuery.isBlank() ||
            student.name.contains(searchQuery, ignoreCase = true) ||
            student.description.contains(searchQuery, ignoreCase = true)
        matchesGrade && matchesQuery
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCaptureClick,
                containerColor = AccentColor,
                contentColor = Color(0xFF052F3F),
                icon = {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null
                    )
                },
                text = {
                    Text(text = "Capturar alumno", fontWeight = FontWeight.Black)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            CollectionHeader(students = students)

            Spacer(modifier = Modifier.height(16.dp))

            // ================= BÚSQUEDA =================
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar alumno...", color = MutedText) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MutedText
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar",
                                tint = MutedText
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = CardColor,
                    unfocusedContainerColor = CardColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ================= FILTRO POR GRADO =================
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    GradeFilterChip(
                        label = "Todos (${students.size})",
                        isSelected = gradeFilter == null,
                        color = AccentColor,
                        onClick = { gradeFilter = null }
                    )
                }

                items(StudentGrade.entries) { grade ->
                    GradeFilterChip(
                        label = "${grade.code} (${students.count { it.grade == grade }})",
                        isSelected = gradeFilter == grade,
                        color = grade.primaryColor,
                        onClick = { gradeFilter = grade }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ================= CONTENIDO =================
            when {
                students.isEmpty() -> EmptyCollectionState(
                    onCaptureClick = onCaptureClick,
                    modifier = Modifier.fillMaxSize()
                )

                filteredStudents.isEmpty() -> NoResultsState(
                    modifier = Modifier.fillMaxSize()
                )

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        StudentCollectionCard(
                            student = student,
                            onClick = { onStudentClick(student) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionHeader(students: List<CapturedStudent>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(AccentColor, SecondaryColor)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📸", fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.size(10.dp))

            Column {
                Text(
                    text = "MI COLECCIÓN",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 23.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Alumnos de la ESOFA capturados",
                    color = MutedText,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBox(
                label = "Total",
                value = students.size.toString(),
                accent = AccentColor,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "AL1",
                value = students.count { it.grade == StudentGrade.AL1 }.toString(),
                accent = StudentGrade.AL1.primaryColor,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "AL2",
                value = students.count { it.grade == StudentGrade.AL2 }.toString(),
                accent = StudentGrade.AL2.primaryColor,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "AL3",
                value = students.count { it.grade == StudentGrade.AL3 }.toString(),
                accent = StudentGrade.AL3.primaryColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardColor)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(
            text = value,
            color = accent,
            fontWeight = FontWeight.Black,
            fontSize = 19.sp
        )
        Text(
            text = label,
            color = MutedText,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GradeFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val background = if (isSelected) {
        Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.65f)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF16304D), CardColor))
    }

    Box(
        modifier = Modifier
            .background(brush = background, shape = RoundedCornerShape(50))
            .border(
                width = 1.dp,
                color = if (isSelected) color else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFF052F3F) else Color(0xFFD6E4F2),
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun EmptyCollectionState(
    onCaptureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🕵️‍♂️", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Tu colección está vacía",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Toma o sube una foto de un alumno, ponle nombre, descripción y su grado: AL1, AL2 o AL3.",
            color = MutedText,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onCaptureClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                contentColor = Color(0xFF052F3F)
            )
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Capturar mi primer alumno", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun NoResultsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🔍", fontSize = 44.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Ningún alumno coincide con la búsqueda",
            color = MutedText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
