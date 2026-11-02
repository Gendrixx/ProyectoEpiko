package com.proyecto.epico.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.epico.model.StudentGrade

/**
 * Píldora con el grado del alumno (AL1, AL2 o AL3).
 */
@Composable
fun GradeBadge(
    grade: StudentGrade,
    modifier: Modifier = Modifier,
    showTitle: Boolean = false
) {
    val pillShape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    listOf(grade.primaryColor, grade.secondaryColor)
                ),
                shape = pillShape
            )
            .border(1.dp, Color.White.copy(alpha = 0.35f), pillShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            tint = grade.badgeTextColor,
            modifier = Modifier.width(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = if (showTitle) "${grade.code} · ${grade.title}" else grade.code,
            color = grade.badgeTextColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.4.sp
        )
    }
}
