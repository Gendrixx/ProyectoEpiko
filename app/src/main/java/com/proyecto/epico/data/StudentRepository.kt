package com.proyecto.epico.data

import android.content.Context
import android.net.Uri
import com.proyecto.epico.model.CapturedStudent
import com.proyecto.epico.model.StudentGrade
import com.proyecto.epico.util.ImageUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Guarda la colección de alumnos en el almacenamiento interno de la app.
 *
 * - Los datos se guardan como JSON en `filesDir/captured_students.json`.
 * - Las fotos se guardan en `filesDir/student_photos/`.
 *
 * No necesita base de datos ni internet: todo queda en el teléfono.
 */
class StudentRepository(private val context: Context) {

    private val storeFile = File(context.filesDir, "captured_students.json")
    private val photosDir = File(context.filesDir, "student_photos").apply { mkdirs() }
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private data class SeedPhoto(
        val assetPath: String,
        val grade: StudentGrade
    )

    private val seedPhotos = listOf(
        SeedPhoto("alumnos/alumno1.jpeg", StudentGrade.AL1),
        SeedPhoto("alumnos/alumno2.jpeg", StudentGrade.AL1),
        SeedPhoto("alumnos/alumno3.jpeg", StudentGrade.AL2),
        SeedPhoto("alumnos/alumno4.jpeg", StudentGrade.AL2),
        SeedPhoto("alumnos/alumno5.jpeg", StudentGrade.AL3)
    )

    /**
     * La primera vez que se abre la app, carga las fotos de ejemplo de la carpeta `assets/alumnos`.
     * El usuario puede cambiarles el nombre y la descripción desde la ficha del alumno.
     */
    fun seedExamplesIfNeeded() {
        if (preferences.getBoolean(KEY_SEEDED, false)) return

        if (load().isNotEmpty()) {
            preferences.edit().putBoolean(KEY_SEEDED, true).apply()
            return
        }

        val seeded = seedPhotos.mapIndexedNotNull { index, seed ->
            val destination = File(photosDir, "ejemplo_${index + 1}.jpg")
            val photoPath = ImageUtils.saveNormalizedAsset(context, seed.assetPath, destination)
                ?: return@mapIndexedNotNull null

            CapturedStudent(
                id = "ejemplo_${index + 1}",
                name = "Alumno ${index + 1}",
                description = "Edita este alumno para ponerle su nombre y descripción.",
                photoPath = photoPath,
                grade = seed.grade,
                capturedAt = System.currentTimeMillis() - index * 86_400_000L
            )
        }

        persist(seeded)
        preferences.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    fun load(): List<CapturedStudent> {
        if (!storeFile.exists()) return emptyList()

        return runCatching {
            val array = JSONArray(storeFile.readText())
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    val photoPath = json.optString("photoPath")
                    // Si la foto ya no existe, el alumno se descarta para no mostrar tarjetas vacías.
                    if (photoPath.isBlank() || !File(photoPath).exists()) continue

                    add(
                        CapturedStudent(
                            id = json.getString("id"),
                            name = json.getString("name"),
                            description = json.optString("description"),
                            photoPath = photoPath,
                            grade = runCatching {
                                StudentGrade.valueOf(json.optString("grade"))
                            }.getOrDefault(StudentGrade.AL1),
                            capturedAt = json.optLong("capturedAt", 0L)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    /**
     * Crea un alumno nuevo: normaliza y guarda la foto con el grado elegido.
     */
    fun create(
        name: String,
        description: String,
        grade: StudentGrade,
        photoUri: Uri
    ): CapturedStudent? {
        val id = UUID.randomUUID().toString()
        val destination = File(photosDir, "$id.jpg")
        val photoPath = ImageUtils.saveNormalizedPhoto(context, photoUri, destination) ?: return null

        val student = CapturedStudent(
            id = id,
            name = name.trim(),
            description = description.trim(),
            photoPath = photoPath,
            grade = grade,
            capturedAt = System.currentTimeMillis()
        )

        persist(load() + student)
        return student
    }

    /**
     * Actualiza el nombre, la descripción y el grado de un alumno ya capturado.
     */
    fun update(
        student: CapturedStudent,
        name: String,
        description: String,
        grade: StudentGrade
    ): CapturedStudent {
        val updated = student.copy(
            name = name.trim(),
            description = description.trim(),
            grade = grade
        )
        persist(load().map { if (it.id == student.id) updated else it })
        return updated
    }

    fun delete(student: CapturedStudent) {
        persist(load().filterNot { it.id == student.id })
        runCatching { File(student.photoPath).delete() }
    }

    private fun persist(students: List<CapturedStudent>) {
        runCatching {
            val array = JSONArray()
            students.forEach { student ->
                array.put(
                    JSONObject().apply {
                        put("id", student.id)
                        put("name", student.name)
                        put("description", student.description)
                        put("photoPath", student.photoPath)
                        put("grade", student.grade.name)
                        put("capturedAt", student.capturedAt)
                    }
                )
            }
            storeFile.writeText(array.toString())
        }
    }

    private companion object {
        const val PREFS_NAME = "colector_alumnos"
        const val KEY_SEEDED = "examples_seeded"
    }
}
