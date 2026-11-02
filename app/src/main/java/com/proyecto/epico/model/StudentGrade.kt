package com.proyecto.epico.model

import androidx.compose.ui.graphics.Color

/**
 * Grado del alumno dentro de la escuela:
 * AL1 (primer año), AL2 (segundo año) y AL3 (tercer año).
 *
 * La paleta usa azules y celestes para mantener la identidad de la app.
 */
enum class StudentGrade(
    val code: String,
    val title: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val badgeTextColor: Color
) {
    AL1(
        code = "AL1",
        title = "Primer Año",
        description = "Alumno en formación inicial.",
        primaryColor = Color(0xFF67E8F9),
        secondaryColor = Color(0xFF0891B2),
        badgeTextColor = Color(0xFF052F3F)
    ),
    AL2(
        code = "AL2",
        title = "Segundo Año",
        description = "Alumno con experiencia y cursos técnicos.",
        primaryColor = Color(0xFF60A5FA),
        secondaryColor = Color(0xFF2563EB),
        badgeTextColor = Color(0xFF06183D)
    ),
    AL3(
        code = "AL3",
        title = "Tercer Año",
        description = "Alumno próximo a graduarse y líder de promoción.",
        primaryColor = Color(0xFF93C5FD),
        secondaryColor = Color(0xFF1D4ED8),
        badgeTextColor = Color.White
    )
}
