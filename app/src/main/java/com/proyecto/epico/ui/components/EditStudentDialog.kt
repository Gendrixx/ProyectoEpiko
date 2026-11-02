package com.proyecto.epico.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.proyecto.epico.model.CapturedStudent
import com.proyecto.epico.model.StudentGrade

/**
 * Diálogo para ponerle nombre, descripción y grado a un alumno ya capturado
 * (por ejemplo, a los alumnos de ejemplo).
 */
@Composable
fun EditStudentDialog(
    student: CapturedStudent,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, grade: StudentGrade) -> Unit
) {
    var name by remember(student.id) { mutableStateOf(student.name) }
    var description by remember(student.id) { mutableStateOf(student.description) }
    var grade by remember(student.id) { mutableStateOf(student.grade) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 24.dp)
                .fillMaxWidth()
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1B31))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "Editar alumno",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 21.sp
                )
                Text(
                    text = "Ponle su nombre y una descripción corta.",
                    color = Color(0xFF9FB3C8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 40) name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre del alumno") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    colors = editFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 160) description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción corta") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    supportingText = {
                        Text(
                            text = "${description.length}/160",
                            color = Color(0xFF6B87A3),
                            fontSize = 10.sp
                        )
                    },
                    colors = editFieldColors()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Grado",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                GradeSelector(
                    selected = grade,
                    onSelect = { grade = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF9FB3C8)
                        )
                    ) {
                        Text(text = "Cancelar")
                    }

                    Button(
                        onClick = {
                            onSave(name.trim(), description.trim(), grade)
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF38BDF8),
                            contentColor = Color(0xFF052F3F),
                            disabledContainerColor = Color(0xFF132A44),
                            disabledContentColor = Color(0xFF6B87A3)
                        )
                    ) {
                        Text(text = "Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF38BDF8),
    unfocusedBorderColor = Color(0xFF1E3A5F),
    focusedContainerColor = Color(0xFF0E1F36),
    unfocusedContainerColor = Color(0xFF0E1F36),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color(0xFF38BDF8),
    unfocusedLabelColor = Color(0xFF9FB3C8),
    cursorColor = Color(0xFF38BDF8)
)
