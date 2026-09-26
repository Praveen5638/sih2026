package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ArtisanViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ArtisanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (viewModel.currentScreen) {
                        AppScreen.MARKETPLACE_HOME -> MarketplaceHomeScreen(viewModel)
                        AppScreen.BUYER_LOGIN -> BuyerAuthScreen(viewModel)
                        AppScreen.BUYER_ORDERS -> BuyerOrdersScreen(viewModel)
                        AppScreen.PUBLIC_PRODUCT_DETAIL -> PublicProductDetailScreen(viewModel)
                        AppScreen.SELLER_LOGIN -> SellerAuthScreen(viewModel)
                        AppScreen.SELLER_PROFILE -> ProfileSetupScreen(viewModel)
                        AppScreen.SELLER_HOME -> HomeScreen(viewModel) // Artisan Dashboard
                        AppScreen.IMAGE_STUDIO, AppScreen.CREATE_PRODUCT -> ImageStudioScreen(viewModel)
                        AppScreen.VOICE_CATALOGER -> VoiceCatalogerScreen(viewModel)
                        AppScreen.AI_REVIEW -> AiReviewScreen(viewModel)
                        AppScreen.PRICING_ASSISTANT -> PricingAssistantScreen(viewModel)
                        AppScreen.FINAL_PREVIEW -> FinalPreviewScreen(viewModel)
                        AppScreen.CATALOG -> CatalogScreen(viewModel)
                        AppScreen.PRODUCT_DETAIL -> ProductDetailScreen(viewModel)
                        else -> MarketplaceHomeScreen(viewModel)
                    }
                }
            }
        }
    }
}
