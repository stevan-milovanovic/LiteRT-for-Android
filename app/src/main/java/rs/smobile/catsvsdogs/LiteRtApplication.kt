package rs.smobile.catsvsdogs

import android.app.Application
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.HiltAndroidApp
import rs.smobile.catsvsdogs.genai.LargeLanguageModel
import rs.smobile.catsvsdogs.genai.LargeLanguageModel.Companion.LLM_NAME_KEY
import rs.smobile.catsvsdogs.genai.LlmDownloadWorker
import javax.inject.Inject

@HiltAndroidApp
class LiteRtApplication : Application() {

    @Inject
    lateinit var largeLanguageModel: LargeLanguageModel

    override fun onCreate() {
        super.onCreate()
        val downloadRequest = OneTimeWorkRequest.Builder(LlmDownloadWorker::class.java)
            .setInputData(workDataOf(LLM_NAME_KEY to largeLanguageModel.name))
            .build()
        WorkManager.getInstance(this).enqueue(downloadRequest)
    }

}