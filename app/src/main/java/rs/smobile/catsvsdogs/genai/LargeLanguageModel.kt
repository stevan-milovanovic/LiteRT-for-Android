package rs.smobile.catsvsdogs.genai

import android.content.Context
import androidx.core.net.toUri
import com.google.mediapipe.tasks.genai.llminference.LlmInference.Backend
import java.io.File


sealed class LargeLanguageModel(
    val name: String,
    val localPath: String,
    val url: String,
    val accessToken: String,
    val preferredBackend: Backend?,
    val temperature: Float,
    val topK: Int,
    val topP: Float,
) {
    companion object {
        const val LLM_NAME_KEY = "LLM_MODEL_NAME"

        fun fromName(name: String): LargeLanguageModel = when (name) {
            Gemma1bItCpu.name -> Gemma1bItCpu
            else -> throw IllegalStateException("Unsupported LLM Model")
        }
    }

    fun isDownloaded(context: Context) = File(path(context)).exists()

    fun path(context: Context) = if (File(localPath).exists()) {
        localPath
    } else {
        pathFromUrl(context)
    }

    fun pathFromUrl(context: Context): String {
        if (url.isNotEmpty()) {
            val urlFileName = url.toUri().lastPathSegment
            if (!urlFileName.isNullOrEmpty()) {
                return File(context.filesDir, urlFileName).absolutePath
            }
        }

        return ""
    }

    object Gemma1bItCpu : LargeLanguageModel(
        name = "Gemma 3 1B IT CPU",
        localPath = "/data/local/tmp/Gemma3-1B-IT_multi-prefill-seq_q8_ekv2048.task",
        url = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/Gemma3-1B-IT_multi-prefill-seq_q8_ekv2048.task",
        accessToken = "ADD HUGGING FACE ACCESS TOKEN HERE",
        preferredBackend = Backend.CPU,
        temperature = 1.0f,
        topK = 64,
        topP = 0.95f
    )

}