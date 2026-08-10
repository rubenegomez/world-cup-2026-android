package com.example.worldcup2026.data.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.worldcup2026.R

object SoundManager {
    private var soundPool: SoundPool? = null
    private var ticSoundId: Int = 0
    private var isLoaded: Boolean = false

    fun init(context: Context) {
        if (soundPool == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == ticSoundId) {
                    isLoaded = true
                }
            }

            try {
                ticSoundId = soundPool?.load(context.applicationContext, R.raw.tic, 1) ?: 0
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playTic() {
        try {
            if (isLoaded && ticSoundId != 0) {
                soundPool?.play(ticSoundId, 0.7f, 0.7f, 1, 0, 1.0f)
            }
        } catch (e: Exception) {
            // Ignorar excepciones de audio en segundo plano
        }
    }
}
