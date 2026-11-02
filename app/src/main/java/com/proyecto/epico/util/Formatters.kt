package com.proyecto.epico.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val captureDateFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"))

/**
 * Formatea la fecha de captura de un alumno, por ejemplo: "3 de mayo de 2026".
 */
fun formatCaptureDate(timestamp: Long): String = captureDateFormat.format(Date(timestamp))
