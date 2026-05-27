package dev.patrickgold.florisboard

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GroqClient {
    // ⚠️ PASTE YOUR ACTUAL GROQ API KEY IN THE QUOTES BELOW
    private const val API_KEY = "gsk_" + "xKP4IMI2OCpctOQQLTKUWGdyb3FY5RFgcMkfEdLSFvNU0CGDuLLh"
    private const val BASE_URL = "https://api.groq.com/openai/v1/chat/completions"
    private val client = OkHttpClient()

    suspend fun rewrite(text: String, mode: RewriteMode): String = withContext(Dispatchers.IO) {
        val systemPrompt = when (mode) {
            RewriteMode.PROOFREAD -> "Fix grammar and spelling only. Return corrected text, nothing else."
            RewriteMode.PARAPHRASE -> "Rephrase this text differently while keeping the meaning. Return only the rephrased text."
            RewriteMode.FORMAL -> "Rewrite this in a formal, professional tone. Return only the rewritten text."
            RewriteMode.CASUAL -> "Rewrite this in a casual, friendly tone. Return only the rewritten text."
        }

        val body = JSONObject().apply {
            put("model", "llama-3.1-8b-instant")
            put("max_tokens", 500)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", text)
                })
            })
        }

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val json = JSONObject(response.body!!.string())
        
        return@withContext json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }
}

enum class RewriteMode { PROOFREAD, PARAPHRASE, FORMAL, CASUAL, CUSTOM }
