package com.example.ai

enum class ConversationStage {
    IDLE,
    LISTENING,
    TRANSCRIBING,
    UNDERSTANDING,
    EXTRACTING,
    ASKING_MISSING_INFO,
    GENERATING_LISTING,
    REVIEWING,
    TTS_REVIEW,
    VOICE_EDITING,
    WAITING_CONFIRMATION,
    SAVING,
    SAVED,
    ERROR
}

data class ListingSession(
    val sessionId: String = "SESS-${System.currentTimeMillis()}",
    val productName: String = "",
    val category: String = "",
    val craft: String = "",
    val material: String = "",
    val color: String = "",
    val technique: String = "",
    val dimensions: String = "Not specified",
    val productionTime: String = "Not specified",
    val productStory: String = "",
    val careInstructions: String = "",
    val transcript: String = "",
    val transcriptHistory: List<String> = emptyList(),
    val descriptionHindi: String = "",
    val descriptionEnglish: String = "",
    val seoTags: String = "",
    val missingFields: List<String> = emptyList(),
    val uncertainFields: List<String> = emptyList(),
    val imageOriginalUri: String = "",
    val imageEnhancedUri: String = "",
    val selectedImageChoice: String = "ENHANCED",
    val currentStage: ConversationStage = ConversationStage.IDLE,
    val currentQuestion: String = "",
    val lastVoiceCommand: String = "",
    val pendingConfirmation: Boolean = false,
    val isRecording: Boolean = false,
    val isSpeakingTTS: Boolean = false
)
