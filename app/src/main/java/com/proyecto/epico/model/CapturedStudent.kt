package com.proyecto.epico.model

/**
 * Alumno capturado en la colección.
 *
 * @param photoPath ruta absoluta de la foto guardada en el almacenamiento interno de la app.
 * @param grade grado del alumno (AL1, AL2 o AL3).
 */
data class CapturedStudent(
    val id: String,
    val name: String,
    val description: String,
    val photoPath: String,
    val grade: StudentGrade,
    val capturedAt: Long
)
