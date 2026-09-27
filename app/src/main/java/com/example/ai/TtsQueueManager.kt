package com.example.ai

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TtsQueueManager {

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val speechQueue = mutableListOf<String>()

    fun playSingleMessage(context: Context, text: String) {
        stopPlayback()
        speechQueue.clear()
        speechQueue.add(text)
        processNextInQueue(context)
    }

    fun playQueue(context: Context, messages: List<String>) {
        stopPlayback()
        speechQueue.clear()
        speechQueue.addAll(messages)
        processNextInQueue(context)
    }

    fun stopPlayback() {
        speechQueue.clear()
        _isSpeaking.value = false
    }

    private fun processNextInQueue(context: Context) {
        if (speechQueue.isEmpty()) {
            _isSpeaking.value = false
            return
        }

        val textToSpeak = speechQueue.removeAt(0)
        _isSpeaking.value = true

        VoiceAssistEngine.initializeTts(context) {
            VoiceAssistEngine.speakReadBack(textToSpeak) {
                if (speechQueue.isNotEmpty()) {
                    processNextInQueue(context)
                } else {
                    _isSpeaking.value = false
                }
            }
        }
    }
}
