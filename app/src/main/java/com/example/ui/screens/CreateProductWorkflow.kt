package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ArtisanViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageStudioScreen(viewModel: ArtisanViewModel) {
    val context = LocalContext.current
    var selectedSample by remember { mutableStateOf("Banarasi Saree") }
    val samples = listOf("Banarasi Saree", "Clay Diya", "Brass Idol", "Terracotta Vase", "Bamboo Basket")

    // Full-resolution temp image file for TakePicture() contract
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }

    // 1. FULL-RESOLUTION CAMERA LAUNCHER (ActivityResultContracts.TakePicture)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraTempUri != null) {
            viewModel.processCameraUriInput(context, cameraTempUri!!) {}
        }
    }

    // 2. MODERN PHOTO PICKER (ActivityResultContracts.PickVisualMedia)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.processCameraUriInput(context, uri) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Studio CV Audit / फोटो स्टूडियो") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.HOME }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LinearProgressIndicator(progress = { 0.25f }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Step 1: Full-Resolution Capture & Objective CV Audit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // USER TRUST MESSAGE BANNER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Product Authenticity Guaranteed 🛡️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Your product stays the same real craft. We improve the photograph.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Real Camera / Gallery Action Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                val tempFile = File(context.cacheDir, "full_res_camera_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
                                cameraTempUri = uri
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Full-Res Camera", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Collections, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Photo Picker", fontSize = 13.sp)
                    }
                }
            }

            // Single Contract Result & Metrics Audit Card
            val contractResult = viewModel.imageContractResult
            if (contractResult != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (contractResult.fallbackTriggered) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📊 Processing Contract Verification", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (contractResult.fallbackTriggered) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                                ) {
                                    Text(
                                        text = if (contractResult.fallbackTriggered) "CONSERVATIVE FALLBACK" else "STUDIO PIPELINE PASSED",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "• Execution Time: ${contractResult.processingTimeMs} ms", fontSize = 12.sp)
                            Text(text = "• Canvas Dimensions: ${contractResult.width} x ${contractResult.height} px (${contractResult.outputFormat})", fontSize = 12.sp)
                            Text(text = "• Encoded File Size: ${(contractResult.outputSizeBytes / 1024)} KB", fontSize = 12.sp)
                            Text(text = "• Protected Mean ΔE_ab: ${String.format("%.2f", contractResult.meanDeltaE)} (95th %: ${String.format("%.2f", contractResult.p95DeltaE)})", fontSize = 12.sp)
                            Text(text = "• Silhouette Jaccard Index: ${(contractResult.jaccardSilhouetteIndex * 100).toInt()}%", fontSize = 12.sp)
                            if (contractResult.fallbackTriggered) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ℹ️ Photo safely retained with original background (limited enhancement). Reason: ${contractResult.fallbackReason}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
                                text = "• Protected Region ΔE_ab Color Drift: ${String.format("%.2f", metrics.protectedColorDriftDeltaE)} (Limit ≤ 5.0)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (metrics.colorFidelityAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            if (metrics.fallbackTriggered) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "⚠️ Fallback Reason: ${metrics.fallbackReason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Before / After Preview Box
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = if (viewModel.enhancedImageUri.isNotBlank()) "Studio Quality Output Ready ✨" else "AI Studio Protected Product: $selectedSample", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = "Background Replaced • Exposure/WB Tuned • Auto Cropped & Centered",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Before / After Choice Controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = viewModel.userSelectedPhotoChoice == "ENHANCED",
                        onClick = { viewModel.userSelectEnhancedPhoto() },
                        label = { Text("Enhanced ✨", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = viewModel.userSelectedPhotoChoice == "ORIGINAL",
                        onClick = { viewModel.userSelectOriginalPhoto() },
                        label = { Text("Original 📷", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Or Select Sample Craft to Test Pipeline:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        samples.take(3).forEach { item ->
                            FilterChip(
                                selected = selectedSample == item,
                                onClick = {
                                    selectedSample = item
                                    val dummyBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
                                    viewModel.processCameraBitmapInput(context, dummyBitmap) {}
                                },
                                label = { Text(item, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Benchmark Trigger & Results Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🧪 10-Craft Empirical Benchmark Suite", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Button(
                                onClick = { viewModel.runStudioBenchmark() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Run Test Suite", fontSize = 11.sp)
                            }
                        }
                        if (viewModel.benchmarkResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Test Results across 10 Craft Categories:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            viewModel.benchmarkResults.forEach { res ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "${res.craftName} (${res.category})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "ΔE_ab: ${String.format("%.1f", res.colorDriftDeltaE)} • Seg: ${(res.segmentationQualityScore * 100).toInt()}% • Time: ${res.processingTimeMs}ms", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = if (res.fallbackTriggered) "FALLBACK 🛡️" else res.passStatus,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (res.fallbackTriggered) Color(0xFFD97706) else Color(0xFF059669)
                                    )
                                }
                                Divider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // CV Pipeline Steps Breakdown Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "⚙️ Studio Pipeline Architecture", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    text = if (metrics != null) "Time: ${metrics.executionTimeMs}ms" else "Ready",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val stages = if (viewModel.studioStageLogs.isNotEmpty()) {
                            viewModel.studioStageLogs
                        } else {
                            listOf(
                                com.example.ai.ProcessingStageLog("REAL CAMERA", "Capture raw frame from device camera"),
                                com.example.ai.ProcessingStageLog("SUBJECT SEGMENTATION", "Isolate product using saliency detection"),
                                com.example.ai.ProcessingStageLog("MASK QUALITY CHECK", "Verify contour area & connectivity"),
                                com.example.ai.ProcessingStageLog("MASK REFINEMENT", "Apply edge blur & anti-aliasing"),
                                com.example.ai.ProcessingStageLog("PRODUCT PROTECTED", "Separate subject from background"),
                                com.example.ai.ProcessingStageLog("BACKGROUND → REPLACE", "Composite onto neutral studio studio background"),
                                com.example.ai.ProcessingStageLog("CV ENHANCEMENT", "Auto exposure, white balance & sharpen"),
                                com.example.ai.ProcessingStageLog("RESIZE + CROP + CENTER", "Autocrop 10% safety margin, center in 1:1"),
                                com.example.ai.ProcessingStageLog("FIDELITY VALIDATION", "Validate zero color distortion"),
                                com.example.ai.ProcessingStageLog("FINAL IMAGE", "Studio-ready high-res PNG generated")
                            )
                        }

                        stages.forEachIndexed { idx, stage ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (viewModel.studioStageLogs.isNotEmpty()) Color(0xFF10B981) else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "${idx + 1}. ${stage.stageName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = stage.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (viewModel.enhancedImageUri.isBlank()) {
                            viewModel.enhanceImage(selectedSample)
                        }
                        viewModel.currentScreen = AppScreen.VOICE_CATALOGER
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("use_photo_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Proceed to Voice Cataloger →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCatalogerScreen(viewModel: ArtisanViewModel) {
    val context = LocalContext.current
    val session = viewModel.listingSession
    var transcriptInput by remember { mutableStateOf(viewModel.voiceTranscript.ifBlank { "Ye Banarasi silk ki saree hai, isme zari ka kaam hai, maroon rang ki hai aur banane mein paanch din lage." }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Business Assistant / वॉयस सहायक") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.IMAGE_STUDIO }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LinearProgressIndicator(progress = { 0.50f }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Step 2: Describe Product by Voice in Your Language", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Speak naturally in Hindi, English, or regional language. Minimal typing needed!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }

            // Large Interactive Microphone Button
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        onClick = {
                            if (session.isRecording) {
                                viewModel.stopVoiceListening()
                            } else {
                                viewModel.startVoiceListening(context) { text ->
                                    transcriptInput = text
                                }
                            }
                        },
                        shape = RoundedCornerShape(50.dp),
                        color = if (session.isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(104.dp),
                        shadowElevation = if (session.isRecording) 12.dp else 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (session.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Record Voice",
                                modifier = Modifier.size(52.dp),
                                tint = if (session.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (session.isRecording) "Listening... Speak now in Hindi / English 🔴" else "Tap Mic & Speak Naturally",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (session.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Quick Voice Presets Chips
            item {
                Column {
                    Text("Sample Artisan Voice Presets:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {
                                transcriptInput = "Ye Banarasi silk ki saree hai, red color, zari ka kaam hai, 5 din lage."
                                viewModel.processConversationalSpeech(context, transcriptInput) {}
                            },
                            label = { Text("Banarasi Saree", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = {
                                transcriptInput = "Mitti ka handcrafted terracotta diya hai, natural red clay, 2 din lage."
                                viewModel.processConversationalSpeech(context, transcriptInput) {}
                            },
                            label = { Text("Terracotta Diya", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = {
                                transcriptInput = "Brass ki handcrafted idol murti hai, golden shine, hand carved polish."
                                viewModel.processConversationalSpeech(context, transcriptInput) {}
                            },
                            label = { Text("Brass Idol", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Live Context-Aware Extracted Slots Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✨ Voice Extracted Product Slots", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { viewModel.speakListingSummary(context) }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Read Back", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "• Product: ${viewModel.productName.ifBlank { "Listening..." }}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "• Material: ${viewModel.material.ifBlank { "Not specified" }}", fontSize = 12.sp)
                        Text(text = "• Color: ${viewModel.color.ifBlank { "Not specified" }}", fontSize = 12.sp)
                        Text(text = "• Technique: ${viewModel.technique.ifBlank { "Not specified" }}", fontSize = 12.sp)
                        Text(text = "• Production Time: ${viewModel.productionTime.ifBlank { "Not specified" }}", fontSize = 12.sp)
                    }
                }
            }

            // Voice Command Edits Row
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Quick Voice Edits:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.processVoiceCommand(context, "Color red karo") },
                            label = { Text("Color Red karo", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.processVoiceCommand(context, "Production time 5 din karo") },
                            label = { Text("Time 5 Din", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.speakListingSummary(context) },
                            label = { Text("🔊 Sunayein", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Transcript Display / Fallback Typing Field
            item {
                OutlinedTextField(
                    value = transcriptInput,
                    onValueChange = { transcriptInput = it },
                    label = { Text("Voice Transcription / Description (या टाइप करें)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("transcript_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                if (viewModel.isAiProcessing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("AI Assistant is extracting structured details & generating bilingual listing...")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.processConversationalSpeech(context, transcriptInput) {
                                viewModel.currentScreen = AppScreen.AI_REVIEW
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("generate_listing_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Confirm & Review AI Listing →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiReviewScreen(viewModel: ArtisanViewModel) {
    var pName by remember { mutableStateOf(viewModel.productName) }
    var descHi by remember { mutableStateOf(viewModel.descriptionHi) }
    var descEn by remember { mutableStateOf(viewModel.descriptionEn) }
    var material by remember { mutableStateOf(viewModel.material) }
    var category by remember { mutableStateOf(viewModel.category) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Listing Review / समीक्षा") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.VOICE_CATALOGER }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LinearProgressIndicator(progress = { 0.75f }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Step 3: Review & Edit AI Generated Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                OutlinedTextField(
                    value = pName,
                    onValueChange = { pName = it },
                    label = { Text("Product Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = material,
                    onValueChange = { material = it },
                    label = { Text("Material") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = descHi,
                    onValueChange = { descHi = it },
                    label = { Text("Hindi Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = descEn,
                    onValueChange = { descEn = it },
                    label = { Text("English Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        viewModel.productName = pName
                        viewModel.descriptionHi = descHi
                        viewModel.descriptionEn = descEn
                        viewModel.material = material
                        viewModel.category = category
                        viewModel.currentScreen = AppScreen.PRICING_ASSISTANT
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("confirm_listing_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Confirm & Proceed to Pricing →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingAssistantScreen(viewModel: ArtisanViewModel) {
    var matCost by remember { mutableStateOf(viewModel.materialCostInput) }
    var labCost by remember { mutableStateOf(viewModel.labourCostInput) }
    var othCost by remember { mutableStateOf(viewModel.otherCostInput) }
    var sellPrice by remember { mutableStateOf(viewModel.sellingPriceInput) }
    var profitMargin by remember { mutableStateOf(40f) } // Interactive margin slider

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dynamic Pricing Assistant / मूल्य सहायक") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.AI_REVIEW }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LinearProgressIndicator(progress = { 0.90f }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Step 4: Cost & Fair Price Recommendation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = matCost,
                        onValueChange = { matCost = it },
                        label = { Text("Material Cost (₹)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = labCost,
                        onValueChange = { labCost = it },
                        label = { Text("Labour Cost (₹)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = othCost,
                    onValueChange = { othCost = it },
                    label = { Text("Transport / Packaging (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Target Profit Margin: ${profitMargin.toInt()}%", fontWeight = FontWeight.Bold)
                        Slider(
                            value = profitMargin,
                            onValueChange = {
                                profitMargin = it
                                val mat = matCost.toDoubleOrNull() ?: 800.0
                                val lab = labCost.toDoubleOrNull() ?: 400.0
                                val oth = othCost.toDoubleOrNull() ?: 120.0
                                val floor = mat + lab + oth
                                val calculated = floor * (1 + (it / 100.0))
                                sellPrice = calculated.toInt().toString()
                            },
                            valueRange = 20f..70f,
                            steps = 10
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("20% (Fair)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("40% (Standard)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("70% (Premium)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.materialCostInput = matCost
                        viewModel.labourCostInput = labCost
                        viewModel.otherCostInput = othCost
                        viewModel.calculatePricing {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recalculate AI Fair Price")
                }
            }

            if (viewModel.pricingResult != null) {
                item {
                    val res = viewModel.pricingResult!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Estimated Cost Floor: ₹${res.costFloor.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Suggested Range: ₹${res.recommendedMin.toInt()} – ₹${res.recommendedMax.toInt()}", fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Recommended Price: ₹${res.recommendedPrice.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = sellPrice,
                    onValueChange = { sellPrice = it },
                    label = { Text("Your Final Selling Price (₹)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selling_price_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        viewModel.sellingPriceInput = sellPrice
                        viewModel.currentScreen = AppScreen.FINAL_PREVIEW
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("proceed_preview_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Review Final Product Listing →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalPreviewScreen(viewModel: ArtisanViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Final Product Preview / पूर्वावलोकन") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.PRICING_ASSISTANT }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LinearProgressIndicator(progress = { 1.0f }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Step 5: Ready for Marketplace & B2B Buyers", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = viewModel.productName.ifBlank { "Handcrafted Artisan Product" }, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (viewModel.userSelectedPhotoChoice == "ENHANCED") "Photo: Enhanced ✨" else "Photo: Original 📷",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Attached Image: ${viewModel.enhancedImageUri.ifBlank { viewModel.originalImageUri.ifBlank { "No image" } }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Category: ${viewModel.category} • Craft: ${viewModel.craft}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Selling Price: ₹${viewModel.sellingPriceInput}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        Text(text = "Hindi Description:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = viewModel.descriptionHi.ifBlank { "पारंपरिक हस्तनिर्मित उत्कृष्ट वस्तु।" }, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "English Description:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = viewModel.descriptionEn.ifBlank { "Premium handmade artisan creation." }, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "SEO Tags: ${viewModel.seoTags}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.saveCurrentProduct(status = "Ready") {
                            viewModel.currentScreen = AppScreen.HOME
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_product_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Publish Product / सहेजें", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
