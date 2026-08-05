package com.example.eduapp.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.eduapp.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val successSoundId: Int
    private val errorSoundId: Int
    private var isEnabled: Boolean = true

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Safely load sounds if they exist
        successSoundId = loadSound(context, "success")
        errorSoundId = loadSound(context, "error")
    }

    private fun loadSound(context: Context, name: String): Int {
        val id = context.resources.getIdentifier(name, "raw", context.packageName)
        return if (id != 0) soundPool.load(context, id, 1) else 0
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun playSuccess() {
        if (isEnabled) soundPool.play(successSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playError() {
        if (isEnabled) soundPool.play(errorSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
