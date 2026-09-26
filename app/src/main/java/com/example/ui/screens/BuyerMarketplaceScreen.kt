package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.data.ProductEntity
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ArtisanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerMarketplaceScreen(viewModel: ArtisanViewModel) {
    val products by viewModel.allProducts.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var checkoutProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var orderSuccessDialog by remember { mutableStateOf(false) }

    val filteredProducts = products.filter { p ->
        val matchesSearch = p.productName.contains(searchQuery, ignoreCase = true) || p.craft.contains(searchQuery, ignoreCase = true)
        val matchesCat = selectedCategory == "All" || p.category.contains(selectedCategory, ignoreCase = true) || p.craft.contains(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCat
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🇮🇳 Artisan e-Marketplace / खरीदार बाज़ार", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("ONDC & B2B Buyer Portal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.currentScreen = AppScreen.HOME }) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Artisan Mode")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search handloom, sarees, idols, pottery...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Handloom", "Terracotta", "Brass", "Painting").forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "No artisan products listed yet.", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.currentScreen = AppScreen.HOME }) {
                            Text("Switch to Artisan Mode & Add Product")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts) { product ->
                        Card(
                            onClick = { checkoutProduct = product },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = product.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                Text(text = product.craft, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "₹${product.sellingPrice.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { checkoutProduct = product },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Buy Now / खरीदें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Checkout / Buy Dialog
        if (checkoutProduct != null) {
            val prod = checkoutProduct!!
            var qty by remember { mutableStateOf("1") }
            var buyerName by remember { mutableStateOf("") }
            var buyerAddress by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { checkoutProduct = null },
                title = { Text("B2B / Direct Buyer Checkout") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = prod.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Price: ₹${prod.sellingPrice.toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = buyerName,
                            onValueChange = { buyerName = it },
                            label = { Text("Buyer / Organization Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = buyerAddress,
                            onValueChange = { buyerAddress = it },
                            label = { Text("Delivery Address / Pincode") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = qty,
                            onValueChange = { qty = it },
                            label = { Text("Quantity") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            checkoutProduct = null
                            orderSuccessDialog = true
                        }
                    ) {
                        Text("Confirm Order / आर्डर दें")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { checkoutProduct = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Order Success Dialog
        if (orderSuccessDialog) {
            AlertDialog(
                onDismissRequest = { orderSuccessDialog = false },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp)) },
                title = { Text("Order Placed Successfully! 🎉") },
                text = { Text("Your B2B / ONDC order has been sent directly to the artisan. Thank you for empowering Indian traditional craftsmanship!") },
                confirmButton = {
                    Button(onClick = { orderSuccessDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
