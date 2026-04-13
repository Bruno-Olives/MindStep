package com.example.mindstep.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import com.example.mindstep.data.local.EntryEntity
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

 fun exportHistoryAsText(context: Context, entries: List<EntryEntity>): Result<Unit> = runCatching {
    val fileName = "mindstep_historico_${timestampForFileName()}.txt"
    val content = buildHistoryLines(entries).joinToString(separator = "\n")
    writeToDownloads(
        context = context,
        fileName = fileName,
        mimeType = "text/plain",
        bytes = content.toByteArray(Charsets.UTF_8)
    )
}

fun exportHistoryAsPdf(context: Context, entries: List<EntryEntity>): Result<Unit> = runCatching {
    val fileName = "mindstep_historico_${timestampForFileName()}.pdf"
    val pdfBytes = buildPdfBytes(buildHistoryLines(entries))
    writeToDownloads(
        context = context,
        fileName = fileName,
        mimeType = "application/pdf",
        bytes = pdfBytes
    )
}

private fun buildHistoryLines(entries: List<EntryEntity>): List<String> {
    val locale = Locale.forLanguageTag("pt-PT")
    val lisbonTimeZone = TimeZone.getTimeZone("Europe/Lisbon")
    val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", locale).apply {
        timeZone = lisbonTimeZone
    }

    return buildList {
        add("Histórico MindStep")
        add("Gerado em: ${dateTimeFormatter.format(Date())}")
        add("")

        entries.forEachIndexed { index, entry ->
            val mood = moodLabels.getOrElse(entry.mood - 1) { entry.mood.toString() }
            val anxiety = anxietyLabels.getOrElse(entry.anxiety - 1) { entry.anxiety.toString() }
            val date = dateTimeFormatter.format(Date(entry.createdAt))

            add("Registo ${index + 1}")
            add("Data: $date")
            add("Humor: $mood (${entry.mood}/5)")
            add("Ansiedade: $anxiety (${entry.anxiety}/5)")
            add("Sono: ${entry.sleep}h")
            add("Passos: ${entry.steps}")
            add("Água: ${entry.waterGlasses} copos")
            add("Notas: ${entry.notes.ifBlank { "Sem notas" }}")
            add("")
        }
    }
}

private fun buildPdfBytes(lines: List<String>): ByteArray {
    val pageWidth = 595
    val pageHeight = 842
    val margin = 40f
    val lineHeight = 20f

    val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 12f
        isAntiAlias = true
    }

    val document = PdfDocument()
    var pageNumber = 1
    var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
    var canvas = page.canvas
    var y = margin

    for (line in lines) {
        val wrappedLines = wrapTextForPdf(line)
        for (wrappedLine in wrappedLines) {
            if (y > pageHeight - margin) {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = margin
            }

            canvas.drawText(wrappedLine, margin, y, textPaint)
            y += lineHeight
        }
    }

    document.finishPage(page)

    return ByteArrayOutputStream().use { output ->
        document.writeTo(output)
        document.close()
        output.toByteArray()
    }
}

private fun wrapTextForPdf(text: String): List<String> {
    val maxChars = 85
    if (text.isBlank() || text.length <= maxChars) return listOf(text)

    val lines = mutableListOf<String>()
    var currentLine = StringBuilder()

    text.split(" ").forEach { word ->
        val nextLength = if (currentLine.isEmpty()) word.length else currentLine.length + 1 + word.length
        if (nextLength > maxChars && currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
            currentLine = StringBuilder(word)
        } else {
            if (currentLine.isNotEmpty()) currentLine.append(" ")
            currentLine.append(word)
        }
    }

    if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
    return if (lines.isEmpty()) listOf(text) else lines
}

private fun timestampForFileName(): String {
    return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
}

private fun writeToDownloads(context: Context, fileName: String, mimeType: String, bytes: ByteArray) {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/MindStep")
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }

    val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
    val fileUri = resolver.insert(collection, values)
        ?: throw IllegalStateException("Não foi possível criar o ficheiro em Downloads.")

    try {
        resolver.openOutputStream(fileUri)?.use { stream ->
            stream.write(bytes)
        } ?: throw IllegalStateException("Não foi possível abrir o ficheiro para escrita.")

        val ready = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
        resolver.update(fileUri, ready, null, null)
    } catch (error: Throwable) {
        resolver.delete(fileUri, null, null)
        throw error
    }
}