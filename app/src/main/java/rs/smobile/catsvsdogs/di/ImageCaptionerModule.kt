package rs.smobile.catsvsdogs.di

import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rs.smobile.catsvsdogs.captioner.ImageCaptioner
import rs.smobile.catsvsdogs.captioner.ImageCaptionerVocabulary

@Module
@InstallIn(SingletonComponent::class)
object ImageCaptionerModule {

    private const val INPUT_SIZE = 346
    private const val MODEL_PATH = "image_captioning.tflite"
    private const val LSTM_MODEL_PATH = "lstm.tflite"
    private const val VOCABULARY_PATH = "vocabulary.txt"

    @Provides
    fun provideImageCaptionerVocabulary(assetManager: AssetManager): ImageCaptionerVocabulary {
        return ImageCaptionerVocabulary(assetManager, VOCABULARY_PATH)
    }

    @Provides
    fun provideImageCaptioner(
        assetManager: AssetManager,
        imageCaptionerVocabulary: ImageCaptionerVocabulary
    ): ImageCaptioner {
        return ImageCaptioner(
            assetManager,
            MODEL_PATH,
            LSTM_MODEL_PATH,
            imageCaptionerVocabulary,
            INPUT_SIZE
        )
    }
} 