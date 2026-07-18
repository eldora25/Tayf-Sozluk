package com.example.kelimehatirlatici

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManager(context: Context) {

    private var muted = false
    private var soundPool: SoundPool? = null
    private var correctSoundId: Int = 0
    private var wrongSoundId: Int = 0
    private var loaded = false

    init {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attr)
            .build()

        soundPool?.setOnLoadCompleteListener { _, _, _ -> loaded = true }

        // Ham ses verisi oluştur — beep sesleri
        correctSoundId = soundPool?.load(context, generateBeepUri(context, 800.0, 0.15, 0.6f), 1) ?: 0
        wrongSoundId = soundPool?.load(context, generateBeepUri(context, 300.0, 0.25, 0.6f), 1) ?: 0
    }

    /** Programatik olarak beep ses dosyası oluştur */
    private fun generateBeepUri(context: Context, frequency: Double, durationSec: Double, volume: Float): android.net.Uri {
        val sampleRate = 44100
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
            samples[i] = (Math.sin(angle) * (volume * Short.MAX_VALUE)).toInt().toShort()
        }

        val byteArray = java.io.ByteArrayOutputStream()
        // WAV header
        val dataSize = numSamples * 2
        byteArray.write("RIFF".toByteArray())
        byteArray.write(intToByteArray(36 + dataSize))
        byteArray.write("WAVE".toByteArray())
        byteArray.write("fmt ".toByteArray())
        byteArray.write(intToByteArray(16))
        byteArray.write(shortToByteArray(1)) // PCM
        byteArray.write(shortToByteArray(1)) // Mono
        byteArray.write(intToByteArray(sampleRate))
        byteArray.write(intToByteArray(sampleRate * 2))
        byteArray.write(shortToByteArray(2))
        byteArray.write(shortToByteArray(16))
        byteArray.write("data".toByteArray())
        byteArray.write(intToByteArray(dataSize))
        for (s in samples) {
            byteArray.write(shortToByteArray(s.toInt()))
        }

        val file = java.io.File(context.cacheDir, "beep_${frequency.toInt()}.wav")
        file.writeBytes(byteArray.toByteArray())
        return android.net.Uri.fromFile(file)
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            (value shr 8 and 0xFF).toByte(),
            (value shr 16 and 0xFF).toByte(),
            (value shr 24 and 0xFF).toByte()
        )
    }

    private fun shortToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            (value shr 8 and 0xFF).toByte()
        )
    }

    var isMuted: Boolean
        get() = muted
        set(value) {
            muted = value
        }

    fun playCorrect() {
        if (!muted && loaded) {
            soundPool?.play(correctSoundId, 1.0f, 1.0f, 1, 0, 1f)
        }
    }

    fun playWrong() {
        if (!muted && loaded) {
            soundPool?.play(wrongSoundId, 1.0f, 1.0f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
