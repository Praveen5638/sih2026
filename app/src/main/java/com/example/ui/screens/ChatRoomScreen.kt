package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConversationEntity
import com.example.data.MessageEntity
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ArtisanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(viewModel: ArtisanViewModel) {
    val context = LocalContext.current
    val conversation by viewModel.activeConversation.collectAsState(initial = null)
    val messages by viewModel.activeMessages.collectAsState(initial = emptyList())
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = conversation?.productName ?: "Negotiation Chat",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (viewModel.userSenderRole == "SELLER")
                                "Buyer: ${conversation?.buyerName ?: "Customer"}"
                            else
                                "Artisan: ${conversation?.artisanName ?: "Artisan"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.userSenderRole == "BUYER") {
                            viewModel.currentScreen = AppScreen.PUBLIC_PRODUCT_DETAIL
                        } else {
                            viewModel.currentScreen = AppScreen.CONVERSATION_LIST
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Role: ${viewModel.userSenderRole}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Voice Reply Button (triggers STT and opens transcript preview sheet)
                    Button(
                        onClick = {
                            if (viewModel.isVoiceReplyRecording) {
                                viewModel.stopVoiceReplyListening()
                            } else {
                                viewModel.startVoiceReplyListening(context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.isVoiceReplyRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = if (viewModel.isVoiceReplyRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (viewModel.isVoiceReplyRecording) "Listening... Tap to Stop / रिकॉर्ड हो रहा है..." else "🎙 Voice Reply / बोलकर जवाब दें",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Text Reply Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Type reply or proposal... / जवाब लिखें...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    viewModel.sendTextMessage(context, textInput)
                                    textInput = ""
                                }
                            },
                            enabled = textInput.isNotBlank(),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Negotiation Terms Pinned Summary Banner
            if (conversation != null) {
                NegotiationSummaryBanner(
                    conversation = conversation!!,
                    onConfirmTerms = {
                        val qty = conversation!!.agreedQuantity.coerceAtLeast(1)
                        val price = if (conversation!!.agreedUnitPrice > 0) conversation!!.agreedUnitPrice else 1000.0
                        viewModel.confirmTerms(context, conversation!!.conversationId, qty, price)
                    }
                )
            }

            // Message History List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        currentUserRole = viewModel.userSenderRole,
                        onSpeakTts = { viewModel.speakMessageWithTts(context, message.text) }
                    )
                }
            }
        }

        // Voice Reply Preview Bottom Sheet Dialog (Mandatory Transcript Review Before Sending)
        if (viewModel.isVoiceReplyPreviewVisible) {
            VoiceReplyPreviewDialog(
                viewModel = viewModel,
                onSend = { viewModel.sendVoiceReplyFromPreview(context) },
                onDismiss = { viewModel.dismissVoiceReplyPreview() }
            )
        }
    }
}

@Composable
private fun NegotiationSummaryBanner(
    conversation: ConversationEntity,
    onConfirmTerms: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Commercial Negotiation Terms",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Qty: ${conversation.agreedQuantity} pcs | Price: ₹${conversation.agreedUnitPrice.toInt()}/pc",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Total Valuation: ₹${(conversation.agreedQuantity * conversation.agreedUnitPrice).toInt()}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (conversation.currentStatus) {
                        "ORDER_READY" -> Color(0xFFD1FAE5)
                        "NEGOTIATING" -> Color(0xFFFEF3C7)
                        else -> Color(0xFFDBEAFE)
                    }
                ) {
                    Text(
                        text = conversation.currentStatus,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (conversation.currentStatus) {
                            "ORDER_READY" -> Color(0xFF065F46)
                            "NEGOTIATING" -> Color(0xFF92400E)
                            else -> Color(0xFF1E40AF)
                        }
                    )
                }
            }

            if (conversation.currentStatus != "ORDER_READY") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onConfirmTerms,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("✅ Confirm & Lock Commercial Terms / शर्तें स्वीकार करें", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageEntity,
    currentUserRole: String,
    onSpeakTts: () -> Unit
) {
    val isMe = message.senderType == currentUserRole

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMe) "You (${message.senderType})" else "${message.senderId} (${message.senderType})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onSpeakTts,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read Aloud",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )

                if (message.extractedQuantity != null || message.extractedPrice != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Terms: ${message.extractedQuantity ?: "?"} pcs @ ₹${message.extractedPrice?.toInt() ?: "?"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (message.status == "PENDING") Icons.Default.Schedule else Icons.Default.DoneAll,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (message.status == "PENDING") "Outbox Sync" else "Synced",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceReplyPreviewDialog(
    viewModel: ArtisanViewModel,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Review Voice Reply / समीक्षा करें")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Verify your transcribed voice message and commercial terms before sending:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = viewModel.voiceReplyTranscript,
                    onValueChange = { viewModel.voiceReplyTranscript = it },
                    label = { Text("Voice Transcript / पाठ") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.extractedQuantityInput,
                        onValueChange = { viewModel.extractedQuantityInput = it },
                        label = { Text("Extracted Qty (pcs)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = viewModel.extractedUnitPriceInput,
                        onValueChange = { viewModel.extractedUnitPriceInput = it },
                        label = { Text("Unit Price (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSend,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Send Reply / भेजें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel / रद्द करें")
            }
        }
    )
}
