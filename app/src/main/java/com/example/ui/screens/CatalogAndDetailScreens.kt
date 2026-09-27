package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ArtisanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(viewModel: ArtisanViewModel) {
    val products by viewModel.allProducts.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredProducts = products.filter { p ->
        val matchesSearch = p.productName.contains(searchQuery, ignoreCase = true) || p.craft.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Draft" -> p.status.equals("Draft", ignoreCase = true)
            "Ready" -> p.status.equals("Ready", ignoreCase = true) || p.status.equals("Published", ignoreCase = true)
            "Published" -> p.status.equals("Published", ignoreCase = true)
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Products Catalog / कैटलॉग") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.SELLER_HOME }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.resetProductDraft()
                            viewModel.currentScreen = AppScreen.CREATE_PRODUCT
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            // Network Status Badge
            NetworkStatusHeader(viewModel)

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Products by name or craft...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Status Filters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Ready", "Published", "Draft").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) }
                    )
                }
            }

            // Empty State Handling
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No products yet / कोई भी उत्पाद सहेजा नहीं गया है",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create your first product using your photo and voice.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.resetProductDraft()
                                    viewModel.currentScreen = AppScreen.CREATE_PRODUCT
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create First Product", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Product Grid List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        Card(
                            onClick = {
                                viewModel.selectedProductIdForDetail = product.id
                                viewModel.currentScreen = AppScreen.PRODUCT_DETAIL
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Product Thumbnail / Placeholder Container
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoFixHigh,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.productName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${product.craft} • ${product.category}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₹${product.sellingPrice.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (product.status.equals("Ready", ignoreCase = true) || product.status.equals("Published", ignoreCase = true)) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                text = product.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (product.status.equals("Ready", ignoreCase = true) || product.status.equals("Published", ignoreCase = true)) Color(0xFF047857) else Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    IconButton(
                                        onClick = {
                                            viewModel.selectedProductIdForDetail = product.id
                                            viewModel.currentScreen = AppScreen.PRODUCT_EDIT
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteProduct(product.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(viewModel: ArtisanViewModel) {
    val products by viewModel.allProducts.collectAsState(initial = emptyList())
    val product = products.find { it.id == viewModel.selectedProductIdForDetail }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details / विवरण") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.CATALOG }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (product != null) {
                        IconButton(onClick = { viewModel.currentScreen = AppScreen.PRODUCT_EDIT }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            viewModel.deleteProduct(product.id)
                            viewModel.currentScreen = AppScreen.CATALOG
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
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
                // Header Image Container
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Artisan Studio Protected Image ✨", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("URI: ${product.enhancedImageUrl.ifBlank { product.originalImageUrl }}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = product.productName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${product.craft} • ${product.category}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (product.status.equals("Ready", ignoreCase = true) || product.status.equals("Published", ignoreCase = true)) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = product.status,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = if (product.status.equals("Ready", ignoreCase = true) || product.status.equals("Published", ignoreCase = true)) Color(0xFF047857) else Color(0xFFB45309),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Explicit Pricing Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "💰 Pricing Breakdown & Economics", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Selling Price:", fontWeight = FontWeight.Medium)
                                Text("₹${product.sellingPrice.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Production Cost Floor:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${product.costFloor.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Market Range (P25–P75):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${product.marketP25.toInt()} – ₹${product.marketP75.toInt()}", fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Recommended Price:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${product.recommendedPrice.toInt()}", fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Text(text = "Hindi Description / हिंदी विवरण", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = product.descriptionHi, fontSize = 13.sp)
                }

                item {
                    Text(text = "English Description / अंग्रेज़ी विवरण", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = product.descriptionEn, fontSize = 13.sp)
                }

                item {
                    Text(text = "Attributes & Specifications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "• Material: ${product.material}\n• Technique: ${product.technique}\n• Color: ${product.color}\n• Dimensions: ${product.dimensions}\n• Production Time: ${product.productionTime}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.currentScreen = AppScreen.PRODUCT_EDIT },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Details")
                        }

                        OutlinedButton(
                            onClick = { /* B2B Share Linkage */ },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Catalog")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(viewModel: ArtisanViewModel) {
    val context = LocalContext.current
    val products by viewModel.allProducts.collectAsState(initial = emptyList())
    val product = products.find { it.id == viewModel.selectedProductIdForDetail }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found")
        }
        return
    }

    var pName by remember { mutableStateOf(product.productName) }
    var craft by remember { mutableStateOf(product.craft) }
    var category by remember { mutableStateOf(product.category) }
    var material by remember { mutableStateOf(product.material) }
    var technique by remember { mutableStateOf(product.technique) }
    var color by remember { mutableStateOf(product.color) }
    var prodTime by remember { mutableStateOf(product.productionTime) }
    var descHi by remember { mutableStateOf(product.descriptionHi) }
    var descEn by remember { mutableStateOf(product.descriptionEn) }
    var sellPrice by remember { mutableStateOf(product.sellingPrice.toInt().toString()) }
    var status by remember { mutableStateOf(product.status) }

    val sellPriceVal = sellPrice.toDoubleOrNull() ?: 0.0
    val isBelowCost = sellPriceVal > 0 && sellPriceVal < product.costFloor

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Product / उत्पाद संपादन") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.PRODUCT_DETAIL }) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(text = "Update Product Attributes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Edits save locally first and trigger outbox sync automatically.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            item {
                OutlinedTextField(
                    value = pName,
                    onValueChange = { pName = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = craft,
                        onValueChange = { craft = it },
                        label = { Text("Craft") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = material,
                        onValueChange = { material = it },
                        label = { Text("Material") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = technique,
                        onValueChange = { technique = it },
                        label = { Text("Technique") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = prodTime,
                        onValueChange = { prodTime = it },
                        label = { Text("Production Time") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = descHi,
                    onValueChange = { descHi = it },
                    label = { Text("Hindi Description") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = descEn,
                    onValueChange = { descEn = it },
                    label = { Text("English Description") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = sellPrice,
                    onValueChange = { sellPrice = it },
                    label = { Text("Selling Price (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (isBelowCost) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "⚠️ Selling price ₹${sellPriceVal.toInt()} is below cost floor ₹${product.costFloor.toInt()}!",
                            color = Color(0xFF991B1B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            item {
                Text("Product Availability Status:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Draft", "Ready", "Published").forEach { st ->
                        FilterChip(
                            selected = status.equals(st, ignoreCase = true),
                            onClick = { status = st },
                            label = { Text(st, fontSize = 11.sp) }
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val updatedProduct = product.copy(
                            productName = pName,
                            craft = craft,
                            category = category,
                            material = material,
                            technique = technique,
                            color = color,
                            productionTime = prodTime,
                            descriptionHi = descHi,
                            descriptionEn = descEn,
                            sellingPrice = sellPriceVal,
                            status = status
                        )
                        viewModel.updateProduct(updatedProduct) {
                            viewModel.currentScreen = AppScreen.PRODUCT_DETAIL
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes (Save-First)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(viewModel: ArtisanViewModel) {
    val products by viewModel.allProducts.collectAsState(initial = emptyList())
    val readyCount = products.count { it.status.equals("Ready", ignoreCase = true) || it.status.equals("Published", ignoreCase = true) }
    val draftCount = products.count { it.status.equals("Draft", ignoreCase = true) }

    var isEditingProfile by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(viewModel.artisanName) }
    var editCraft by remember { mutableStateOf(viewModel.artisanCraft) }
    var editLocation by remember { mutableStateOf(viewModel.artisanLocation) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artisan Storefront Profile / दुकान") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = AppScreen.SELLER_HOME }) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ARTISAN STOREFRONT HEADER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = viewModel.artisanName.take(1).uppercase(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = viewModel.artisanName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${viewModel.artisanCraft} • ${viewModel.artisanLocation}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { isEditingProfile = !isEditingProfile },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Profile Details", fontSize = 11.sp)
                        }
                    }
                }
            }

            if (isEditingProfile) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Edit Artisan Profile", fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Artisan Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editCraft,
                                onValueChange = { editCraft = it },
                                label = { Text("Craft / Specialization") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editLocation,
                                onValueChange = { editLocation = it },
                                label = { Text("Location") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    viewModel.artisanName = editName
                                    viewModel.artisanCraft = editCraft
                                    viewModel.artisanLocation = editLocation
                                    isEditingProfile = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Profile")
                            }
                        }
                    }
                }
            }

            // STOREFRONT METRICS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${products.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Total Catalog", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$readyCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text(text = "Ready to Sell", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$draftCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            Text(text = "Drafts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ARTISAN STOREFRONT PRODUCT CATALOG GRID
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Storefront Digital Catalog", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.currentScreen = AppScreen.CATALOG }) {
                        Text("Manage Catalog")
                    }
                }
            }

            if (products.isEmpty()) {
                item {
                    Text(text = "No products listed in your digital storefront yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                items(products) { product ->
                    Card(
                        onClick = {
                            viewModel.selectedProductIdForDetail = product.id
                            viewModel.currentScreen = AppScreen.PRODUCT_DETAIL
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = product.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "${product.craft} • ₹${product.sellingPrice.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}
