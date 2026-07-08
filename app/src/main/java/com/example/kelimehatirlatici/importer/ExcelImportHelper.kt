package com.example.kelimehatirlatici.importer

import android.content.Context
import android.net.Uri
import com.example.kelimehatirlatici.data.Word
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.WorkbookFactory

object ExcelImportHelper {

    fun importFromExcel(
        context: Context,
        uri: Uri
    ): List<Word> {
        val words = mutableListOf<Word>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (rowIndex in 1..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue

                val word = row.getCell(0).asText()
                val meaning = row.getCell(1).asText()
                val example = row.getCell(2).asText()
                val library = row.getCell(3).asText().ifBlank { "İçe Aktarılan Excel" }
                val level = row.getCell(4).asText().ifBlank { "Genel" }

                if (word.isNotBlank() && meaning.isNotBlank()) {
                    words.add(
                        Word(
                            word = word,
                            meaning = meaning,
                            example = example,
                            library = library,
                            level = level
                        )
                    )
                }
            }

            workbook.close()
        }

        return words
    }

    private fun Cell?.asText(): String {
        return when (this?.cellType) {
            org.apache.poi.ss.usermodel.CellType.STRING -> this.stringCellValue.trim()
            org.apache.poi.ss.usermodel.CellType.NUMERIC -> this.numericCellValue.toString()
            org.apache.poi.ss.usermodel.CellType.BOOLEAN -> this.booleanCellValue.toString()
            org.apache.poi.ss.usermodel.CellType.FORMULA -> this.toString().trim()
            else -> ""
        }
    }
}
