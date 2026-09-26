package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ArtisanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageStudioScreen(viewModel: ArtisanViewModel) {
    var selectedSample by remember { mutableStateOf("Banarasi Saree") }
    val samples = listOf("Banarasi Saree", "Clay Diya", "Brass Idol", "Terracotta Vase", "Bamboo Basket")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Image Studio / फोटो स्टूडियो") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.HOME }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(progress = { 0.25f }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Step 1: Capture or Select Product Photo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Before / After Preview Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "AI Enhanced: $selectedSample", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Background Removed • Lighting Corrected • Crisp Edges", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Choose Sample Artisan Item or Upload:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                samples.take(3).forEach { item ->
                    FilterChip(
                        selected = selectedSample == item,
                        onClick = { selectedSample = item },
                        label = { Text(item, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Simulated Retake */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retake")
                }

                Button(
                    onClick = {
                        viewModel.enhanceImage(selectedSample)
                        viewModel.currentScreen = AppScreen.VOICE_CATALOGER
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("use_photo_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Use Photo →")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCatalogerScreen(viewModel: ArtisanViewModel) {
    var transcriptInput by remember { mutableStateOf(viewModel.voiceTranscript.ifBlank { "Ye Banarasi silk ki saree hai, isme zari ka kaam hai, maroon rang ki hai aur banane mein paanch din lage." }) }
    var isRecording by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multilingual Auto-Cataloger / वॉयस कैटलॉग") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.IMAGE_STUDIO }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(progress = { 0.50f }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Step 2: Describe Product in Your Language", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Speak or type naturally in Hindi, English, or regional language", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Mic Button with Recording State
            Surface(
                onClick = { isRecording = !isRecording },
                shape = RoundedCornerShape(50.dp),
                color = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(100.dp),
                shadowElevation = if (isRecording) 8.dp else 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Record",
                        modifier = Modifier.size(48.dp),
                        tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isRecording) "Recording voice... Tap to stop 🔴" else "Tap to Record Voice Note",
                fontWeight = FontWeight.Bold,
                color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Quick Sample Voice Presets
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { transcriptInput = "Ye Banarasi silk ki saree hai, zari ka kaam hai." }, label = { Text("Saree") })
                AssistChip(onClick = { transcriptInput = "Mitti ka handcrafted diya hai, Diwali special." }, label = { Text("Diya") })
                AssistChip(onClick = { transcriptInput = "Brass ki shandar murti hai, handmade polish." }, label = { Text("Brass Idol") })
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = transcriptInput,
                onValueChange = { transcriptInput = it },
                label = { Text("Voice Transcription / Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("transcript_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (viewModel.isAiProcessing) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("AI is extracting structured product details...")
            } else {
                Button(
                    onClick = {
                        viewModel.processVoiceTranscript(transcriptInput) {
                            viewModel.currentScreen = AppScreen.AI_REVIEW
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("generate_listing_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Generate AI Listing / लिस्टिंग बनाएं", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        Text(text = viewModel.productName.ifBlank { "Handcrafted Artisan Product" }, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
