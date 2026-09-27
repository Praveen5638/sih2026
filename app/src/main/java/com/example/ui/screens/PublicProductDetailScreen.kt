package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ArtisanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProductDetailScreen(viewModel: ArtisanViewModel) {
    val products by viewModel.allProducts.collectAsState(initial = emptyList())
    val product = products.find { it.id == viewModel.selectedProductIdForDetail }
    var buyDialog by remember { mutableStateOf(false) }
    var successDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details / विवरण") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.MARKETPLACE_HOME }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (product == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Product not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item {
                    Text(text = product.productName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${product.craft} • ${product.category}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Direct Price:", fontWeight = FontWeight.Medium)
                                Text("₹${product.sellingPrice.toInt()}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Production Time:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(product.productionTime)
                            }
                        }
                    }
                }

                item {
                    Text(text = "Hindi Description / विवरण", fontWeight = FontWeight.Bold)
                    Text(text = product.descriptionHi.ifBlank { "पारंपरिक हस्तनिर्मित उत्कृष्ट वस्तु।" }, fontSize = 14.sp)
                }

                item {
                    Text(text = "English Description", fontWeight = FontWeight.Bold)
                    Text(text = product.descriptionEn.ifBlank { "Premium handmade artisan masterpiece." }, fontSize = 14.sp)
                }

                item {
                    Text(text = "Craft Specifications", fontWeight = FontWeight.Bold)
                    Text(text = "Material: ${product.material} | Technique: ${product.technique} | Color: ${product.color}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                item {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.openOrCreateConversationForProduct(context, product, isBuyer = true)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("💬 Voice / Text Negotiate with Artisan", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (!viewModel.isBuyerLoggedIn) {
                                    viewModel.currentScreen = AppScreen.BUYER_LOGIN
                                } else {
                                    buyDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (viewModel.isBuyerLoggedIn) "Buy Now / खरीदें (₹${product.sellingPrice.toInt()})" else "Login to Buy / खरीदें", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (buyDialog) {
            var address by remember { mutableStateOf("New Delhi, Pincode 110001") }
            var qty by remember { mutableStateOf("1") }

            AlertDialog(
                onDismissRequest = { buyDialog = false },
                title = { Text("Complete Purchase") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Buyer: ${viewModel.buyerName} (${viewModel.buyerMobile})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Delivery Address") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.placeBuyerOrder(product!!.productName, product.sellingPrice, qty.toIntOrNull() ?: 1, address)
                        buyDialog = false
                        successDialog = true
                    }) {
                        Text("Place Order & Track")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { buyDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (successDialog) {
            AlertDialog(
                onDismissRequest = { successDialog = false },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp)) },
                title = { Text("Order Placed Successfully!") },
                text = { Text("Your order has been placed and added to 'My Orders' for real-time tracking.") },
                confirmButton = {
                    Button(onClick = {
                        successDialog = false
                        viewModel.currentScreen = AppScreen.BUYER_ORDERS
                    }) { Text("View My Orders") }
                }
            )
        }
    }
}
