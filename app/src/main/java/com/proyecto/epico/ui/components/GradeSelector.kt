package com.proyecto.epico.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.epico.model.StudentGrade

/**
 * Selector de grado con tres opciones: AL1, AL2 y AL3.
 */
@Composable
fun GradeSelector(
    selected: StudentGrade,
    onSelect: (StudentGrade) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StudentGrade.entries.forEach { grade ->
            val isSelected = grade == selected
            val shape = RoundedCornerShape(14.dp)
            val background = if (isSelected) {
                Brush.horizontalGradient(listOf(grade.primaryColor, grade.secondaryColor))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF10253D), Color(0xFF0E1F36)))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(brush = background, shape = shape)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) grade.primaryColor else Color.White.copy(alpha = 0.12f),
                        shape = shape
                    )
                    .clickable { onSelect(grade) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = grade.code,
                    color = if (isSelected) grade.badgeTextColor else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = grade.title,
                    color = if (isSelected) {
                        grade.badgeTextColor.copy(alpha = 0.85f)
                    } else {
                        Color(0xFF9FB3C8)
                    },
                    fontSize = 9.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
