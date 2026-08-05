package com.example.eduapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eduapp.data.PuzzleRepository
import com.example.eduapp.util.SoundManager

class AppViewModelFactory(
    private val repository: PuzzleRepository,
    private val soundManager: SoundManager? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository, soundManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
