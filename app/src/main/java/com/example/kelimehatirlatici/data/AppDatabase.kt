package com.example.kelimehatirlatici.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Word::class,
        StudyStats::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kelime_hatirlatici_database"
                )
                .fallbackToDestructiveMigration()  // ← KRİTİK: Eski veritabanını silip yeniden oluşturur
                .build()

                INSTANCE = instance
                instance
            }
        }

        @JvmStatic
        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }
}
