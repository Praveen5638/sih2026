package com.example.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SupabaseApiService {

    // ============================================================
    // POSTGREST REST API: PRODUCTS
    // ============================================================
    @GET("rest/v1/products")
    suspend fun getProducts(
        @Query("select") select: String = "*",
        @Query("status") status: String? = null,
        @Query("order") order: String = "updated_at.desc"
    ): Response<List<SupabaseProductDto>>

    @POST("rest/v1/products")
    suspend fun insertProduct(
        @Header("Prefer") prefer: String = "return=representation",
        @Body product: SupabaseProductDto
    ): Response<List<SupabaseProductDto>>

    @PATCH("rest/v1/products")
    suspend fun updateProduct(
        @Query("local_id") localIdEq: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body product: SupabaseProductDto
    ): Response<List<SupabaseProductDto>>

    // ============================================================
    // POSTGREST REST API: CONVERSATIONS
    // ============================================================
    @GET("rest/v1/conversations")
    suspend fun getConversations(
        @Query("select") select: String = "*",
        @Query("order") order: String = "updated_at.desc"
    ): Response<List<SupabaseConversationDto>>

    @POST("rest/v1/conversations")
    suspend fun upsertConversation(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body conversation: SupabaseConversationDto
    ): Response<List<SupabaseConversationDto>>

    @PATCH("rest/v1/conversations")
    suspend fun updateConversationStatus(
        @Query("conversation_id") conversationIdEq: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body updates: Map<String, String>
    ): Response<List<SupabaseConversationDto>>

    // ============================================================
    // POSTGREST REST API: MESSAGES
    // ============================================================
    @GET("rest/v1/messages")
    suspend fun getMessagesForConversation(
        @Query("conversation_id") conversationIdEq: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.asc"
    ): Response<List<SupabaseMessageDto>>

    @POST("rest/v1/messages")
    suspend fun insertMessage(
        @Header("Prefer") prefer: String = "return=representation",
        @Body message: SupabaseMessageDto
    ): Response<List<SupabaseMessageDto>>

    // ============================================================
    // SUPABASE STORAGE API: PRODUCT IMAGES BUCKET
    // ============================================================
    @POST("storage/v1/object/product-images/{filename}")
    suspend fun uploadProductImage(
        @Path("filename") filename: String,
        @Header("Content-Type") contentType: String = "image/jpeg",
        @Body imageBytes: RequestBody
    ): Response<ResponseBody>
}
