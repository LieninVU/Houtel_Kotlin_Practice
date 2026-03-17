package com.example.hotel_app.data.parser

import android.content.Context
import com.example.hotel_app.domain.model.Event
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

    fun parseFromAssets(fileName: String = "events.xlsx"): List<Event> {
        return try {
            context.assets.open(fileName).use { inputStream ->
                if (fileName.endsWith(".xlsx")) parseXlsx(inputStream)
                else parseXls(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getMockEvents() // fallback на моки если файл не найден
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
        Event("Вечер живой музыки", "Джазовый концерт в лобби", "15.06.2024", "19:00", "Лобби", "Концерт"),
        Event("Экскурсия по городу", "Обзорная экскурсия с гидом", "16.06.2024", "10:00", "Главный вход", "Экскурсия"),
        Event("Йога на рассвете", "Утренняя йога на террасе", "17.06.2024", "07:00", "Терраса", "Спорт"),
        Event("Дегустация вин", "Вечер с сомелье", "18.06.2024", "20:00", "Ресторан", "Гастрономия"),
        Event("Мастер-класс по кулинарии", "Готовим с шеф-поваром", "19.06.2024", "15:00", "Кухня", "Мастер-класс")
    )
}
