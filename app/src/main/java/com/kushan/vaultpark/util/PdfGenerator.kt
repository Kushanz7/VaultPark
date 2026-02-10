package com.kushan.vaultpark.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.model.ShiftReport
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points (approx)
    private const val PAGE_HEIGHT = 842 // A4 height in points (approx)
    private const val MARGIN = 40
    private const val CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN)

    private val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val subtitlePaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }

    private val headerPaint = Paint().apply {
        color = Color.BLACK
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val linePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
    }

    fun generateSessionReportPdf(context: Context, sessions: List<ParkingSession>): File? {
        val pdfDocument = PdfDocument()
        val file = File(context.getExternalFilesDir(null), "parking_report_${System.currentTimeMillis()}.pdf")

        try {
            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            var yPosition = MARGIN.toFloat()

            // Title
            canvas.drawText("VAULTPARK - PARKING SESSIONS REPORT", (PAGE_WIDTH / 2).toFloat(), yPosition, titlePaint)
            yPosition += 25
            canvas.drawText("Generated: ${formatDateTime(System.currentTimeMillis())}", (PAGE_WIDTH / 2).toFloat(), yPosition, subtitlePaint)
            yPosition += 20
            canvas.drawText("Total Sessions: ${sessions.size}", (PAGE_WIDTH / 2).toFloat(), yPosition, subtitlePaint)
            yPosition += 40

            // Table Headers
            drawSessionTableHeader(canvas, yPosition)
            yPosition += 20
            canvas.drawLine(MARGIN.toFloat(), yPosition, (PAGE_WIDTH - MARGIN).toFloat(), yPosition, linePaint)
            yPosition += 10

            // Rows
            for (session in sessions) {
                if (yPosition > PAGE_HEIGHT - MARGIN) {
                    pdfDocument.finishPage(page)
                    currentPageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN.toFloat()
                    
                    // Re-draw headers on new page
                    drawSessionTableHeader(canvas, yPosition)
                    yPosition += 20
                    canvas.drawLine(MARGIN.toFloat(), yPosition, (PAGE_WIDTH - MARGIN).toFloat(), yPosition, linePaint)
                    yPosition += 10
                }

                drawSessionRow(canvas, yPosition, session)
                yPosition += 25 // Row height
                canvas.drawLine(MARGIN.toFloat(), yPosition, (PAGE_WIDTH - MARGIN).toFloat(), yPosition, linePaint)
                yPosition += 15 // Gap between rows
            }

            pdfDocument.finishPage(page)

            // Save file
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            return file

        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawSessionTableHeader(canvas: Canvas, y: Float) {
        val colWidths = floatArrayOf(80f, 60f, 60f, 70f, 100f, 80f)
        var currentX = MARGIN.toFloat()

        val headers = listOf("Date", "Gate", "Vehicle", "Duration", "Status", "Notes")
        
        headers.forEachIndexed { index, header ->
             canvas.drawText(header, currentX, y, headerPaint)
             currentX += colWidths.getOrElse(index) { 80f }
        }
    }

    private fun drawSessionRow(canvas: Canvas, y: Float, session: ParkingSession) {
        val colWidths = floatArrayOf(80f, 60f, 60f, 70f, 100f, 80f)
        var currentX = MARGIN.toFloat()

        val data = listOf(
            formatDate(session.entryTime),
            session.gateLocation,
            session.vehicleNumber,
            if (session.exitTime != null) calculateDuration(session.entryTime, session.exitTime) else "Active",
            session.status,
            session.notes.take(20) // Truncate notes
        )

        data.forEachIndexed { index, text ->
            val truncatedText = if (text.length > 15 && index != 5) text.take(12) + "..." else text
            canvas.drawText(truncatedText, currentX, y, textPaint)
             currentX += colWidths.getOrElse(index) { 80f }
        }
    }
    
    // --- Helper Functions ---

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun calculateDuration(entryTime: Long, exitTime: Long): String {
        val durationMinutes = (exitTime - entryTime) / (1000 * 60)
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return "${hours}h ${minutes}m"
    }
}
