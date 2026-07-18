package com.example.kelimehatirlatici

import android.content.Context
import android.media.ToneGenerator
import android.media.AudioManager

class SoundManager(context: Context) {

    private var muted = false
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50)

    var isMuted: Boolean
        get() = muted
        set(value) {
            muted = value
        }

    fun playCorrect() {
        if (!muted) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150)
        }
    }

    fun playWrong() {
        if (!muted) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 150)
        }
    }

    fun release() {
        toneGenerator.release()
    }
}
