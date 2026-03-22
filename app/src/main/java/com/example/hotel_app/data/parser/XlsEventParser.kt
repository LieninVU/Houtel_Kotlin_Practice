package com.example.hotel_app.data.parser

import android.content.Context
import com.example.hotel_app.R
import com.example.hotel_app.ResourceProvider
import com.example.hotel_app.domain.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream

/**
 * Парсер XLS/XLSX файлов с мероприятиями отеля.
 *
 * Ожидаемая структура файла (первая строка — заголовки):
 * | title | description | date | time | location | category |
 *
 * Файл должен лежать в assets/events.xls или events.xlsx
 */
class XlsEventParser(private val context: Context) {

    /**
     * Парсинг файла из assets.
     * Выполняется на Dispatchers.IO для избежания блокировки Main потока.
     */
    suspend fun parseFromAssets(fileName: String = "events.xlsx"): List<Event> {
        // ✅ Явное указание Dispatchers.IO для IO-операций
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream = context.assets.open(fileName)
                if (fileName.endsWith(".xlsx")) parseXlsx(inputStream)
                else parseXls(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                getMockEvents() // fallback на моки если файл не найден
            }
        }
    }

    private fun parseXlsx(inputStream: InputStream): List<Event> {
        val workbook = XSSFWorkbook(inputStream)
        return parseSheet(workbook)
    }

    private fun parseXls(inputStream: InputStream): List<Event> {
        val workbook = HSSFWorkbook(inputStream)
        return parseSheet(workbook)
    }

    private fun parseSheet(workbook: org.apache.poi.ss.usermodel.Workbook): List<Event> {
        val sheet = workbook.getSheetAt(0)
        val events = mutableListOf<Event>()

        // Пропускаем первую строку (заголовки), начинаем с 1
        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue
            try {
                val event = Event(
                    title = row.getCell(0)?.toString().orEmpty(),
                    description = row.getCell(1)?.toString().orEmpty(),
                    date = row.getCell(2)?.toString().orEmpty(),
                    time = row.getCell(3)?.toString().orEmpty(),
                    location = row.getCell(4)?.toString().orEmpty(),
                    category = row.getCell(5)?.toString().orEmpty()
                )
                if (event.title.isNotBlank()) events.add(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        workbook.close()
        return events
    }

    /** Моки для работы без XLS-файла */
    fun getMockEvents(): List<Event> = listOf(
        Event(
            ResourceProvider.getString(R.string.event_music_evening_name),
            ResourceProvider.getString(R.string.event_music_evening_description),
            ResourceProvider.getString(R.string.event_music_evening_date),
            ResourceProvider.getString(R.string.event_music_evening_time),
            ResourceProvider.getString(R.string.event_music_evening_location),
            ResourceProvider.getString(R.string.event_music_evening_category)
        ),
        Event(
            ResourceProvider.getString(R.string.event_city_tour_name),
            ResourceProvider.getString(R.string.event_city_tour_description),
            ResourceProvider.getString(R.string.event_city_tour_date),
            ResourceProvider.getString(R.string.event_city_tour_time),
            ResourceProvider.getString(R.string.event_city_tour_location),
            ResourceProvider.getString(R.string.event_city_tour_category)
        ),
        Event(
            ResourceProvider.getString(R.string.event_yoga_dawn_name),
            ResourceProvider.getString(R.string.event_yoga_dawn_description),
            ResourceProvider.getString(R.string.event_yoga_dawn_date),
            ResourceProvider.getString(R.string.event_yoga_dawn_time),
            ResourceProvider.getString(R.string.event_yoga_dawn_location),
            ResourceProvider.getString(R.string.event_yoga_dawn_category)
        ),
        Event(
            ResourceProvider.getString(R.string.event_wine_tasting_name),
            ResourceProvider.getString(R.string.event_wine_tasting_description),
            ResourceProvider.getString(R.string.event_wine_tasting_date),
            ResourceProvider.getString(R.string.event_wine_tasting_time),
            ResourceProvider.getString(R.string.event_wine_tasting_location),
            ResourceProvider.getString(R.string.event_wine_tasting_category)
        ),
        Event(
            ResourceProvider.getString(R.string.event_cooking_masterclass_name),
            ResourceProvider.getString(R.string.event_cooking_masterclass_description),
            ResourceProvider.getString(R.string.event_cooking_masterclass_date),
            ResourceProvider.getString(R.string.event_cooking_masterclass_time),
            ResourceProvider.getString(R.string.event_cooking_masterclass_location),
            ResourceProvider.getString(R.string.event_cooking_masterclass_category)
        )
    )
}
