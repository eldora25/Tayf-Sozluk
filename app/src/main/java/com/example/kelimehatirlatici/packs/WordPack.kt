package com.example.kelimehatirlatici.packs

data class WordPack(
    val name: String,
    val description: String,
    val language: String,
    val targetLanguage: String,
    val level: String,
    val words: List<WordPackItem>
)

data class WordPackItem(
    val word: String,
    val meaning: String,
    val example: String = ""
)
