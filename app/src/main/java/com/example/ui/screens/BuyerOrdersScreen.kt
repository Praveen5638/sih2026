package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.viewmodel.ArtisanViewModel

@Composable
fun BuyerOrdersScreen(viewModel: ArtisanViewModel) {
    OrderHistoryScreen(viewModel)
}
