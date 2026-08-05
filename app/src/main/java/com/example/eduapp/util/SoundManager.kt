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
    private val loadedSounds = java.util.Collections.synchronizedSet(mutableSetOf<Int>())

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
            }
            android.util.Log.d("SoundManager", "Sound loaded: $sampleId, status: $status")
        }

        // Load sounds directly using R.raw
        successSoundId = soundPool.load(context, R.raw.success, 1)
        errorSoundId = soundPool.load(context, R.raw.error, 1)
        
        android.util.Log.d("SoundManager", "IDs: success=$successSoundId, error=$errorSoundId")
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun playSuccess() {
        android.util.Log.d("SoundManager", "playSuccess called. isEnabled=$isEnabled, successSoundId=$successSoundId, loaded=${loadedSounds.contains(successSoundId)}")
        if (isEnabled && loadedSounds.contains(successSoundId)) {
            val result = soundPool.play(successSoundId, 1f, 1f, 1, 0, 1f)
            android.util.Log.d("SoundManager", "playSuccess result: $result")
        }
    }

    fun playError() {
        android.util.Log.d("SoundManager", "playError called. isEnabled=$isEnabled, errorSoundId=$errorSoundId, loaded=${loadedSounds.contains(errorSoundId)}")
        if (isEnabled && loadedSounds.contains(errorSoundId)) {
            val result = soundPool.play(errorSoundId, 1f, 1f, 1, 0, 1f)
            android.util.Log.d("SoundManager", "playError result: $result")
        }
    }

    fun release() {
        soundPool.release()
    }
}
