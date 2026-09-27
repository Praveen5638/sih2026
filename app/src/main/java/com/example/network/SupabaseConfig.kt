package com.example.network

object SupabaseConfig {
    // Supabase Project Endpoint & Client Publishable Key
    // Never hardcode secret service_role keys. Only public client publishable keys are safe for mobile clients.
    var SUPABASE_URL: String = "https://your-supabase-project.supabase.co"
    var SUPABASE_ANON_KEY: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.client-publishable-anon-key-placeholder"

    fun configure(url: String, anonKey: String) {
        if (url.isNotBlank()) SUPABASE_URL = url
        if (anonKey.isNotBlank()) SUPABASE_ANON_KEY = anonKey
    }
}
