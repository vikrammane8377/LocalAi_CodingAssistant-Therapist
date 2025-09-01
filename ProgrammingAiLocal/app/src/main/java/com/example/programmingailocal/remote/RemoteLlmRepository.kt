package com.example.programmingailocal.remote

import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor

object RemoteLlmRepository {

    private const val HF_TOKEN = ""
    // TODO: Replace with your actual Space URL
    private const val SPACE_ENDPOINT = "https://vikrammane-qwen25-therapy-bot.hf.space/api/predict/"

    private val json = Json { ignoreUnknownKeys = true }

    private val client: OkHttpClient by lazy {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val auth = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $HF_TOKEN")
                .build()
            chain.proceed(req)
        }
        OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(auth)
            .build()
    }

    /**
     * Sends a chat prompt plus history to the HF Space and returns the assistant reply.
     * @param prompt  Latest user message
     * @param history Ordered list of previous (user, assistant) turns
     */
    suspend fun generate(prompt: String, history: List<Pair<String, String>>): String {
        val bodyJson = buildJsonObject {
            putJsonArray("data") { add(prompt) }
            put("api_name", JsonPrimitive("/chat_fn"))
        }
        val mediaType = "application/json".toMediaType()
        val reqBody = bodyJson.toString().toRequestBody(mediaType)
        val req = Request.Builder().url(SPACE_ENDPOINT).post(reqBody).build()
        val resp = client.newCall(req).execute()
        if (!resp.isSuccessful) throw IllegalStateException("HTTP ${resp.code}")
        val respBody = resp.body?.string() ?: throw IllegalStateException("empty body")
        val root = json.parseToJsonElement(respBody).jsonObject
        val dataArr = root["data"]?.jsonArray ?: error("invalid response")
        // `data` already is the reply string in this schema
        val reply = dataArr[0].jsonPrimitive.content
        return reply
    }
} 