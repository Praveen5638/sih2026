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
fun MarketplaceHomeScreen(viewModel: ArtisanViewModel) {
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
                        Text("🇮🇳 Artisan e-Marketplace", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(if (viewModel.isBuyerLoggedIn) "Welcome, ${viewModel.buyerName}" else "ONDC & Direct Buyer Portal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    if (viewModel.isBuyerLoggedIn) {
                        IconButton(onClick = { viewModel.currentScreen = AppScreen.BUYER_ORDERS }) {
                            Icon(Icons.Default.LocalShipping, contentDescription = "My Orders", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        TextButton(onClick = { viewModel.currentScreen = AppScreen.BUYER_LOGIN }) {
                            Text("Buyer Login", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedButton(
                        onClick = { viewModel.currentScreen = AppScreen.SELLER_LOGIN },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Seller Login", fontSize = 12.sp)
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
            // Hero Banner for Buyers
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Discover Authentic Indian Crafts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Directly empower artisans & track your orders seamlessly.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                    if (viewModel.isBuyerLoggedIn) {
                        Button(
                            onClick = { viewModel.currentScreen = AppScreen.BUYER_ORDERS },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("My Orders (${viewModel.buyerOrders.size})")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search sarees, pottery, brass idols, crafts...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Handloom", "Terracotta", "Brass", "Pottery").forEach { cat ->
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
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "No artisan products listed yet.", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.currentScreen = AppScreen.SELLER_LOGIN }) {
                            Text("Are you an artisan? List Products here →")
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
                            onClick = {
                                viewModel.selectedProductIdForDetail = product.id
                                viewModel.currentScreen = AppScreen.PUBLIC_PRODUCT_DETAIL
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.selectedProductIdForDetail = product.id
                                            viewModel.currentScreen = AppScreen.PUBLIC_PRODUCT_DETAIL
                                        },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Details", fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = {
                                            if (!viewModel.isBuyerLoggedIn) {
                                                viewModel.currentScreen = AppScreen.BUYER_LOGIN
                                            } else {
                                                checkoutProduct = product
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Buy", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Buy Dialog
        if (checkoutProduct != null) {
            val prod = checkoutProduct!!
            var buyerAddress by remember { mutableStateOf("New Delhi, Pincode 110001") }
            var qty by remember { mutableStateOf("1") }

            AlertDialog(
                onDismissRequest = { checkoutProduct = null },
                title = { Text("Direct Buyer / B2B Checkout") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = prod.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Price: ₹${prod.sellingPrice.toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(text = "Logged in Buyer: ${viewModel.buyerName} (${viewModel.buyerMobile})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = buyerAddress, onValueChange = { buyerAddress = it }, label = { Text("Delivery Address / Pincode") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.placeBuyerOrder(prod.productName, prod.sellingPrice, qty.toIntOrNull() ?: 1, buyerAddress)
                            checkoutProduct = null
                            orderSuccessDialog = true
                        }
                    ) {
                        Text("Confirm Order & Track / आर्डर दें")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { checkoutProduct = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (orderSuccessDialog) {
            AlertDialog(
                onDismissRequest = { orderSuccessDialog = false },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp)) },
                title = { Text("Order Placed Successfully! 🎉") },
                text = { Text("Your order has been placed and added to 'My Orders' for real-time ONDC tracking.") },
                confirmButton = {
                    Button(onClick = {
                        orderSuccessDialog = false
                        viewModel.currentScreen = AppScreen.BUYER_ORDERS
                    }) {
                        Text("View My Orders")
                    }
                }
            )
        }
    }
}
