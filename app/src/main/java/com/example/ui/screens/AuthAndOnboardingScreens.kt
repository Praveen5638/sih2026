package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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

@Composable
fun SplashScreen(viewModel: ArtisanViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(96.dp),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Handyman,
                        contentDescription = "App Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Artisan AI",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SIH 2026 PS-26090 — Digital Business Assistant",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = { viewModel.currentScreen = AppScreen.LANGUAGE },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .testTag("get_started_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Get Started / शुरू करें", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(viewModel: ArtisanViewModel) {
    val languages = listOf(
        "हिन्दी (Hindi)" to "Hindi",
        "English" to "English",
        "বাংলা (Bengali)" to "Bengali",
        "தமிழ் (Tamil)" to "Tamil",
        "తెలుగు (Telugu)" to "Telugu"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Language / भाषा चुनें") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose your preferred language for voice and cataloging",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            languages.forEach { (label, code) ->
                val isSelected = viewModel.selectedLanguage == code
                Card(
                    onClick = { viewModel.selectedLanguage = code },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        RadioButton(selected = isSelected, onClick = { viewModel.selectedLanguage = code })
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.currentScreen = AppScreen.AUTH },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("continue_language_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Continue / आगे बढ़ें", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: ArtisanViewModel) {
    var phoneInput by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artisan Login / कारीगर लॉगिन") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.LANGUAGE }) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (!otpSent) "Enter Mobile Number" else "Enter OTP Sent via SMS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (!otpSent) "We will send a 4-digit verification code" else "Code sent to +91 $phoneInput",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (!otpSent) {
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Mobile Number (मोबाइल नंबर)") },
                    leadingIcon = { Text("+91 ", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mobile_input")
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { if (phoneInput.length >= 10) otpSent = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("send_otp_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Send OTP / ओटीपी भेजें", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { otpInput = it },
                    label = { Text("4-Digit OTP") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("otp_input")
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.mobileNumber = phoneInput
                        viewModel.currentScreen = AppScreen.PROFILE
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("verify_otp_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Verify & Login / सत्यापित करें", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(viewModel: ArtisanViewModel) {
    var name by remember { mutableStateOf(viewModel.artisanName) }
    var craft by remember { mutableStateOf(viewModel.artisanCraft) }
    var location by remember { mutableStateOf(viewModel.artisanLocation) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artisan Profile / प्रोफाइल") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Tell us about your craft so AI can assist you better", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Artisan Full Name / पूरा नाम") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("artisan_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = craft,
                onValueChange = { craft = it },
                label = { Text("Craft / Occupation (कला / शिल्प)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("artisan_craft_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("State / Location (स्थान)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("artisan_location_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.artisanName = name
                    viewModel.artisanCraft = craft
                    viewModel.artisanLocation = location
                    viewModel.currentScreen = AppScreen.HOME
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Start Digital Assistant / शुरू करें", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
