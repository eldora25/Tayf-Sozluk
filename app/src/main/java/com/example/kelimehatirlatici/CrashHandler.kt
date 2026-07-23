package com.example.kelimehatirlatici

import android.os.Process
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(private val defaultHandler: Thread.UncaughtExceptionHandler?) :
    Thread.UncaughtExceptionHandler {

    companion object {
        private const val TAG = "TayfSozlukCrash"
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Crash logunu detaylı olarak yazdır
        Log.e(TAG, "═══════════════════════════════════════")
        Log.e(TAG, "❌ UYGULAMA ÇÖKTÜ!")
        Log.e(TAG, "Thread: ${thread.name}")
        Log.e(TAG, "Hata: ${throwable.message}")
        Log.e(TAG, "Sınıf: ${throwable.javaClass.name}")
        Log.e(TAG, "═══════════════════════════════════════")

        // Stack trace'i yazdır
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        Log.e(TAG, "Stack Trace:\n${sw.toString()}")

        // Zincirleme hataları yazdır
        var cause = throwable.cause
        var causeIndex = 1
        while (cause != null) {
            Log.e(TAG, "Neden $causeIndex: ${cause.message}")
            val csw = StringWriter()
            val cpw = PrintWriter(csw)
            cause.printStackTrace(cpw)
            cpw.flush()
            Log.e(TAG, "Neden $causeIndex Stack Trace:\n${csw.toString()}")
            cause = cause.cause
            causeIndex++
        }

        Log.e(TAG, "═══════════════════════════════════════")

        // Varsayılan crash handler'a yönlendir (uygulama yine kapanır ama log alınmış olur)
        defaultHandler?.uncaughtException(thread, throwable) ?: run {
            Process.killProcess(Process.myPid())
            System.exit(1)
        }
    }
}
