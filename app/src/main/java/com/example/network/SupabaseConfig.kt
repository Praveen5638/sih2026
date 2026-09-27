package com.example.network

object SupabaseConfig {
    // Supabase Project Endpoint & Modern Client Publishable Key (sb_publishable_...)
    // Never hardcode secret keys (sb_secret_...) or service_role keys. Only client publishable keys are safe for mobile clients.
    var SUPABASE_URL: String = "https://nfoanbypjuzqjjxawbjp.supabase.co"
    var SUPABASE_ANON_KEY: String = "sb_publishable_uLXd02e0kyazm5KKs1n3tA_X_xmM-JJ"

    fun configure(url: String, anonKey: String) {
        if (url.isNotBlank()) SUPABASE_URL = url
        if (anonKey.isNotBlank()) SUPABASE_ANON_KEY = anonKey
    }
}


