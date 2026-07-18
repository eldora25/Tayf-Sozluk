package com.example.kelimehatirlatici

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManager(context: Context) {

    private var muted = false
    private var soundPool: SoundPool? = null
    private var correctSoundId: Int = 0
    private var wrongSoundId: Int = 0

    init {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attr)
            .build()

        // Sistem seslerini yükle
        correctSoundId = soundPool?.load(context, android.R.raw.fallbackring, 1) ?: 0
        wrongSoundId = soundPool?.load("/system/media/audio/ui/Error.ogg", 1) ?: 0
    }

    var isMuted: Boolean
        get() = muted
        set(value) {
            muted = value
        }

    fun playCorrect() {
        if (!muted) {
            soundPool?.play(correctSoundId, 0.3f, 0.3f, 1, 0, 1f)
        }
    }

    fun playWrong() {
        if (!muted) {
            soundPool?.play(wrongSoundId, 0.3f, 0.3f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
