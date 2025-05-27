package rs.smobile.catsvsdogs.genai

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import rs.smobile.catsvsdogs.genai.LargeLanguageModel.Companion.LLM_NAME_KEY
import java.io.File
import java.io.FileOutputStream

class LlmDownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private companion object {
        private val TAG = LlmDownloadWorker::class.simpleName
    }

    override suspend fun doWork() = try {
        val modelName = inputData.getString(LLM_NAME_KEY)
        val model = LargeLanguageModel.fromName(modelName ?: return Result.failure())
        downloadModel(model, context)
        Log.i(TAG, "Large Language Model downloaded successfully")
        Result.success()
    } catch (e: Exception) {
        Log.e(TAG, "Large Language Model download failed due to", e)
        Result.failure()
    }

    private fun downloadModel(
        model: LargeLanguageModel,
        context: Context
    ) {
        if (model.isDownloaded(context)) {
            Log.d(TAG, "Large Language Model Already Downloaded")
            return
        }
        val requestBuilder = Request.Builder()
            .url(model.url)
            .addHeader("Authorization", "Bearer ${model.accessToken}")

        val outputFile = File(model.pathFromUrl(context))
        val response = OkHttpClient().newCall(requestBuilder.build()).execute()
        if (!response.isSuccessful) {
            throw Exception("Large Language Model Download failed: ${response.code}")
        }

        response.body?.byteStream()?.use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead = 0L
                val contentLength = response.body?.contentLength() ?: -1
                var currentProgress = 0
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    val progress = if (contentLength > 0) {
                        (totalBytesRead * 100 / contentLength).toInt()
                    } else {
                        -1
                    }
                    if (progress % 10 == 0 && currentProgress != progress) {
                        Log.d(TAG, "Large Language Model Download Progress: $progress%...")
                        currentProgress = progress
                    }
                }
                outputStream.flush()
            }
        }
    }

}