package rs.smobile.catsvsdogs.genai

import android.content.Context
import android.util.Log
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession.LlmInferenceSessionOptions
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import javax.inject.Inject

class LlmDescriptor @Inject constructor(
    private val context: Context,
    private val model: LargeLanguageModel,
) {

    private companion object {
        private const val MAX_TOKENS = 100
        private val TAG = LlmDescriptor::class.simpleName
    }

    private lateinit var llmInference: LlmInference
    private lateinit var llmInferenceSession: LlmInferenceSession

    fun generateResponseAsync(
        prompt: String,
        progressListener: ProgressListener<String>
    ): ListenableFuture<String> {
        if (!::llmInference.isInitialized) {
            if (model.isDownloaded(context)) {
                llmInference = createEngine(context)
            } else {
                return SettableFuture.create()
            }
        }
        if (::llmInferenceSession.isInitialized) {
            llmInferenceSession.close()
        }
        llmInferenceSession = createSession()

        llmInferenceSession.addQueryChunk(prompt)
        return llmInferenceSession.generateResponseAsync(progressListener)
    }

    fun getModelName() = model.name

    private fun createSession(): LlmInferenceSession = try {
        LlmInferenceSession.createFromOptions(llmInference, getSessionOptions())
    } catch (e: Exception) {
        Log.e(TAG, "LlmInferenceSession create error: ${e.message}", e)
        throw IllegalStateException("Failed to create model session")
    }

    private fun getSessionOptions() = LlmInferenceSessionOptions.builder()
        .setTemperature(model.temperature)
        .setTopK(model.topK)
        .setTopP(model.topP)
        .build()

    private fun createEngine(context: Context) = try {
        LlmInference.createFromOptions(context, getInferenceOptions())
    } catch (e: Exception) {
        Log.e(TAG, "Load model error: ${e.message}", e)
        throw IllegalStateException("Failed to load model")
    }

    private fun getInferenceOptions() = LlmInference.LlmInferenceOptions.builder()
        .setModelPath(model.path(context))
        .setMaxTokens(MAX_TOKENS)
        .apply { model.preferredBackend?.let { setPreferredBackend(it) } }
        .build()

}