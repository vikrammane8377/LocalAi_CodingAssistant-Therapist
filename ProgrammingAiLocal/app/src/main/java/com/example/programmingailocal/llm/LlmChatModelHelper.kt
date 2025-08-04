package com.example.programmingailocal.llm

import android.content.Context
import android.util.Log
import com.example.programmingailocal.data.*
import com.example.programmingailocal.util.cleanUpMediapipeTaskErrorMessage
import com.google.mediapipe.tasks.genai.llminference.GraphOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession

// Callback returning partial text results. "done" will be true on the final chunk.
typealias ResultListener = (partialResult: String, done: Boolean) -> Unit

private const val TAG = "LlmChatModelHelper"

// Therapist persona prompt (injected once per new session)
private const val THERAPIST_SYSTEM_PROMPT = """
You are a compassionate, experienced, licensed therapist conducting your first session with a new client.

Session Guidelines:
• Prioritize empathy—acknowledge and validate the client's emotions genuinely.
• Ask thoughtful, open-ended questions that gently encourage introspection.
• When the client shares positive news or achievements, warmly celebrate and affirm their progress briefly.
• When addressing struggles or challenges, suggest one or two practical coping strategies, or offer alternative perspectives to consider.
• Respond naturally, as a human therapist would, avoiding repetitive or overly formal language.
• Maintain responses concise (under 220 words), conversational, and comforting.
• Conclude each message with a supportive, curiosity-driven question that invites the client to share deeper thoughts or feelings.
"""

// Holder that we keep inside the Model.instance field
data class LlmModelInstance(
    val engine: LlmInference,
    var session: LlmInferenceSession,
    var systemPromptInjected: Boolean = false,
)

object LlmChatModelHelper {

    fun initialize(context: Context, model: Model, onDone: (String?) -> Unit) {
        Log.d(TAG, "initialize() for model ${model.name}  path=${model.getPath(context)}")
        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(model.getPath(context))
                .setMaxTokens(DEFAULT_MAX_TOKEN)
                .setPreferredBackend(LlmInference.Backend.CPU)
                .build()

            val inference = LlmInference.createFromOptions(context, options)
            Log.d(TAG, "LlmInference engine created")
            val session = LlmInferenceSession.createFromOptions(
                inference,
                LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTopK(DEFAULT_TOPK)
                    .setTopP(DEFAULT_TOPP)
                    .setTemperature(DEFAULT_TEMPERATURE)
                    .setGraphOptions(GraphOptions.builder().setEnableVisionModality(false).build())
                    .build()
            )
            model.instance = LlmModelInstance(inference, session)
            Log.d(TAG, "Session created; initialization complete")
            onDone(null)
        } catch (e: Exception) {
            val cleaned = cleanUpMediapipeTaskErrorMessage(e.message ?: "Unknown error")
            Log.e(TAG, "Initialization error", e)
            onDone(cleaned)
        }
    }

    fun runInference(model: Model, input: String, resultListener: ResultListener) {
        val instance = model.instance as? LlmModelInstance ?: return
        val session = instance.session

        Log.d(TAG, "runInference input='${input.take(30)}' len=${input.length}")

        // Inject system prompt once per session so it influences the dialogue without repetition
        if (!instance.systemPromptInjected) {
            session.addQueryChunk(THERAPIST_SYSTEM_PROMPT)
            instance.systemPromptInjected = true
        }

        session.addQueryChunk(input)
        Log.d(TAG, "Query chunk added; starting generateResponseAsync…")
        session.generateResponseAsync { partialText, done ->
            Log.d(TAG, "partial len=${partialText.length} done=$done")
            resultListener(partialText, done)
        }
    }

    fun resetSession(model: Model) {
        val instance = model.instance as? LlmModelInstance ?: return
        var retries = 0
        while (retries < 5) {
            try {
                instance.session.close()
                val newSession = LlmInferenceSession.createFromOptions(
                    instance.engine,
                    LlmInferenceSession.LlmInferenceSessionOptions.builder().build()
                )
                instance.session = newSession
                return
            } catch (e: IllegalStateException) {
                // Previous invocation still processing; wait then retry
                Log.e(TAG, "Reset attempt ${retries+1} failed – generation in progress")
                Thread.sleep(200)
                retries++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reset session", e)
                return
            }
        }
        Log.e(TAG, "Could not reset session after retries")
    }

    fun cleanUp(model: Model) {
        val instance = model.instance as? LlmModelInstance ?: return
        try {
            instance.session.close()
            instance.engine.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to close resources", e)
        }
        model.instance = null
    }
} 