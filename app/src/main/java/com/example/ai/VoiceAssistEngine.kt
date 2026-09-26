package com.example.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

data class ExtractedSlotData(
    val productName: String? = null,
    val material: String? = null,
    val color: String? = null,
    val technique: String? = null,
    val productionTime: String? = null,
    val category: String? = null,
    val craft: String? = null
)

data class ClassifiedVoiceCommand(
    val intent: String, // "EDIT_FIELD", "SAVE_PRODUCT", "NAVIGATE_PRICING", "REPEAT_TTS", "CONFIRM", "CANCEL"
    val targetField: String? = null, // "productName", "material", "color", "technique", "productionTime", "descriptionEn"
    val newValue: String? = null
)

object VoiceAssistEngine {

    private var speechRecognizer: SpeechRecognizer? = null
    private var ttsEngine: TextToSpeech? = null
    private var isTtsInitialized = false

    // Craft Vocabulary Lookup for Phonetic Correction
    private val craftVocabulary = listOf(
        "Banarasi", "Chikankari", "Dhokra", "Zari", "Terracotta",
        "Handloom", "Pottery", "Brass", "Bamboo", "Embroidery",
        "Weaving", "Wood carving", "Metal craft", "Bandhani", "Phulkari"
    )

    fun initializeTts(context: Context, onInit: () -> Unit = {}) {
        if (ttsEngine == null) {
            ttsEngine = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = ttsEngine?.setLanguage(Locale("hi", "IN"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        ttsEngine?.language = Locale.ENGLISH
                    }
                    isTtsInitialized = true
                    onInit()
                }
            }
        }
    }

    // Audio Turn Management: Stop TTS cleanly BEFORE opening microphone
    fun stopTts() {
        if (isTtsInitialized && ttsEngine?.isSpeaking == true) {
            ttsEngine?.stop()
        }
    }

    fun speakReadBack(text: String, onComplete: () -> Unit = {}) {
        stopTts()
        if (isTtsInitialized && ttsEngine != null) {
            val params = Bundle()
            ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "UTTERANCE_READBACK")
        }
    }

    fun startListening(
        context: Context,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Enforce audio turn rule: Never allow TTS speaking + Mic listening simultaneously
        stopTts()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available on this device.")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_PATH_OBSERVED_INTENTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Kuch bolna sunai nahi diya. Phir se koshish karein."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout ho gaya. Microphone tap karein."
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                    else -> "Voice recognition Error: $error"
                }
                onError(msg)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val rawTranscript = matches[0]
                    val corrected = applyCraftVocabularyCorrection(rawTranscript)
                    onResult(corrected)
                } else {
                    onError("No speech captured.")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    // Apply Craft Vocabulary Phonetic Corrections
    private fun applyCraftVocabularyCorrection(rawText: String): String {
        var result = rawText
        for (term in craftVocabulary) {
            if (result.contains(term, ignoreCase = true)) {
                // Ensure correct craft capitalization & spelling
                result = result.replace(Regex("(?i)\\b$term\\b"), term)
            }
        }
        return result
    }

    // Extract Context-Aware Slots from Natural Speech (e.g. "Banarasi saree, red color, zari work, 5 din")
    fun extractSlotsFromSpeech(transcript: String, craftType: String): ExtractedSlotData {
        val lower = transcript.lowercase()

        val material = when {
            lower.contains("silk") || lower.contains("रेशम") -> "Silk"
            lower.contains("cotton") || lower.contains("सूती") -> "Cotton"
            lower.contains("brass") || lower.contains("पीतल") -> "Brass"
            lower.contains("clay") || lower.contains("मिट्टी") || lower.contains("terracotta") -> "Terracotta / Clay"
            lower.contains("wood") || lower.contains("लकड़ी") -> "Carved Wood"
            lower.contains("bamboo") || lower.contains("बांस") -> "Natural Bamboo"
            else -> null
        }

        val color = when {
            lower.contains("red") || lower.contains("लाल") || lower.contains("maroon") -> "Red / Maroon"
            lower.contains("gold") || lower.contains("गोल्डन") || lower.contains("सोनहरा") -> "Golden Gold"
            lower.contains("yellow") || lower.contains("पीला") -> "Yellow"
            lower.contains("blue") || lower.contains("नीला") -> "Royal Blue"
            lower.contains("green") || lower.contains("हरा") -> "Emerald Green"
            lower.contains("black") || lower.contains("काला") -> "Black"
            else -> null
        }

        val technique = when {
            lower.contains("zari") || lower.contains("जारी") || lower.contains("जरी") -> "Zari Handweave"
            lower.contains("handloom") || lower.contains("हथकरघा") -> "Traditional Handloom"
            lower.contains("handmade") || lower.contains("हस्तनिर्मित") -> "Handcrafted / Handmade"
            lower.contains("carved") || lower.contains("नक्काशी") -> "Hand Carved"
            lower.contains("embroidery") || lower.contains("कढ़ाई") -> "Hand Embroidery"
            else -> null
        }

        val prodTime = when {
            lower.contains("5 din") || lower.contains("पांच दिन") || lower.contains("5 days") -> "5 Days"
            lower.contains("3 din") || lower.contains("तीन दिन") || lower.contains("3 days") -> "3 Days"
            lower.contains("7 din") || lower.contains("सात दिन") || lower.contains("7 days") || lower.contains("1 haft") -> "7 Days"
            lower.contains("2 din") || lower.contains("दो दिन") -> "2 Days"
            else -> null
        }

        val pName = when {
            lower.contains("saree") || lower.contains("साड़ी") -> "Banarasi Silk Saree"
            lower.contains("diya") || lower.contains("दिया") -> "Handcrafted Terracotta Diya"
            lower.contains("idol") || lower.contains("मूर्ति") -> "Brass Crafts Idol"
            lower.contains("vase") || lower.contains("गमला") -> "Handmade Earthen Vase"
            else -> "Handcrafted $craftType"
        }

        return ExtractedSlotData(
            productName = pName,
            material = material,
            color = color,
            technique = technique,
            productionTime = prodTime,
            category = "Traditional Craft",
            craft = craftType
        )
    }

    // Natural Voice Command Classifier (e.g., "Color red karo", "Save karo", "Pricing par chalo")
    fun classifyVoiceCommand(commandText: String): ClassifiedVoiceCommand {
        val lower = commandText.lowercase()

        return when {
            lower.contains("save") || lower.contains("सहेजें") || lower.contains("save karo") -> {
                ClassifiedVoiceCommand(intent = "SAVE_PRODUCT")
            }
            lower.contains("pricing") || lower.contains("price") || lower.contains("कीमत") || lower.contains("pricing par chalo") -> {
                ClassifiedVoiceCommand(intent = "NAVIGATE_PRICING")
            }
            lower.contains("phir se sunao") || lower.contains("repeat") || lower.contains("सुनो") -> {
                ClassifiedVoiceCommand(intent = "REPEAT_TTS")
            }
            lower.contains("haan") || lower.contains("yes") || lower.contains("sahi hai") || lower.contains("confirm") -> {
                ClassifiedVoiceCommand(intent = "CONFIRM")
            }
            lower.contains("color") || lower.contains("रंग") -> {
                val newVal = commandText.replace(Regex("(?i).*color\\s*"), "").replace(Regex("(?i).*रंग\\s*"), "").replace("karo", "").trim()
                ClassifiedVoiceCommand(intent = "EDIT_FIELD", targetField = "color", newValue = newVal.ifBlank { "Red" })
            }
            lower.contains("material") || lower.contains("सामग्री") -> {
                val newVal = commandText.replace(Regex("(?i).*material\\s*"), "").replace("karo", "").trim()
                ClassifiedVoiceCommand(intent = "EDIT_FIELD", targetField = "material", newValue = newVal.ifBlank { "Silk" })
            }
            lower.contains("naam") || lower.contains("name") || lower.contains("नाम") -> {
                val newVal = commandText.replace(Regex("(?i).*naam\\s*"), "").replace(Regex("(?i).*name\\s*"), "").replace("karo", "").trim()
                ClassifiedVoiceCommand(intent = "EDIT_FIELD", targetField = "productName", newValue = newVal.ifBlank { "Handmade Saree" })
            }
            lower.contains("production time") || lower.contains("समय") || lower.contains("din") -> {
                val newVal = commandText.replace(Regex("(?i).*time\\s*"), "").replace("karo", "").trim()
                ClassifiedVoiceCommand(intent = "EDIT_FIELD", targetField = "productionTime", newValue = newVal.ifBlank { "5 Days" })
            }
            else -> {
                ClassifiedVoiceCommand(intent = "EDIT_FIELD", targetField = "general", newValue = commandText)
            }
        }
    }
}
