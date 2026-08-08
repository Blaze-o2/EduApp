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
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
                // Pre-warm the sound by playing it at 0 volume once
                soundPool.play(sampleId, 0f, 0f, 0, 0, 1f)
            }
            android.util.Log.d("SoundManager", "Sound loaded: $sampleId, status: $status")
        }

        // Load sounds using AssetFileDescriptor
        successSoundId = loadSound(context, R.raw.success)
        errorSoundId = loadSound(context, R.raw.error)
        
        android.util.Log.d("SoundManager", "IDs: success=$successSoundId, error=$errorSoundId")
    }

    private fun loadSound(context: Context, resId: Int): Int {
        return try {
            val afd = context.resources.openRawResourceFd(resId)
            soundPool.load(afd, 1)
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Failed to load sound $resId", e)
            0
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun playSuccess() {
        if (!isEnabled) return
        
        // SoundPool.play can fail if called too quickly after load or if the pool is busy.
        // We use a high priority (1) and max volume (1.0f).
        val streamId = soundPool.play(successSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        if (streamId == 0) {
            android.util.Log.w("SoundManager", "playSuccess failed, retrying once...")
            // Small delay fallback for hardware that needs a moment to initialize the stream
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                soundPool.play(successSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            }, 100)
        }
    }

    fun playError() {
        if (!isEnabled) return
        
        val streamId = soundPool.play(errorSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        if (streamId == 0) {
            android.util.Log.w("SoundManager", "playError failed, retrying once...")
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                soundPool.play(errorSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            }, 100)
        }
    }

    fun release() {
        soundPool.release()
    }
}
