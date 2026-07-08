package com.example.kelimehatirlatici.packs

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object WordPackReader {

    fun readAllPacks(context: Context): List<WordPack> {
        val packs = mutableListOf<WordPack>()

        val assetManager = context.assets
        val files = assetManager.list("packs") ?: emptyArray()

        files
            .filter { it.endsWith(".json") }
            .forEach { fileName ->
                val jsonText = assetManager
                    .open("packs/$fileName")
                    .bufferedReader()
                    .use { it.readText() }

                packs.add(parsePack(jsonText))
            }

        return packs
    }

    private fun parsePack(jsonText: String): WordPack {
        val json = JSONObject(jsonText)

        val wordsArray = json.getJSONArray("words")
        val words = mutableListOf<WordPackItem>()

        for (i in 0 until wordsArray.length()) {
            val item = wordsArray.getJSONObject(i)

            words.add(
                WordPackItem(
                    word = item.getString("word"),
                    meaning = item.getString("meaning"),
                    example = item.optString("example", "")
                )
            )
        }

        return WordPack(
            name = json.getString("name"),
            description = json.optString("description", ""),
            language = json.optString("language", "en"),
            targetLanguage = json.optString("targetLanguage", "tr"),
            level = json.optString("level", "Genel"),
            words = words
        )
    }
}
