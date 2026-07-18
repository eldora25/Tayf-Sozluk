package com.example.kelimehatirlatici

import android.content.Context

class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var quizQuestionCount: Int
        get() = prefs.getInt("quiz_question_count", 10)
        set(value) = prefs.edit().putInt("quiz_question_count", value).apply()

    var randomOrder: Boolean
        get() = prefs.getBoolean("random_order", true)
        set(value) = prefs.edit().putBoolean("random_order", value).apply()

    var memorizationThreshold: Int
        get() = prefs.getInt("memorization_threshold", 3)
        set(value) = prefs.edit().putInt("memorization_threshold", value).apply()

    var darkMode: Boolean
        get() = prefs.getBoolean("dark_mode", false)
        set(value) = prefs.edit().putBoolean("dark_mode", value).apply()

    // ── YENİ: Kalıcı kütüphane ve seviye ──
    var selectedLibrary: String
        get() = prefs.getString("selected_library", "İngilizce A1") ?: "İngilizce A1"
        set(value) = prefs.edit().putString("selected_library", value).apply()

    var selectedLevel: String
        get() = prefs.getString("selected_level", "A1") ?: "A1"
        set(value) = prefs.edit().putString("selected_level", value).apply()
}
