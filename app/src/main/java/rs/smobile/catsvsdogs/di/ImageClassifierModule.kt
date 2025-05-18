package rs.smobile.catsvsdogs.di

import android.content.Context
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import rs.smobile.catsvsdogs.classifier.ImageClassifier


@Module
@InstallIn(SingletonComponent::class)
object ImageClassifierModule {

    private const val INPUT_SIZE = 224
    private const val LITERT_MODEL_PATH = "catsvsdogs.tflite"
    private const val LITERT_LABEL_PATH = "label.txt"

    @Provides
    fun provideAssetManager(@ApplicationContext context: Context): AssetManager {
        return context.assets
    }

    @Provides
    fun provideImageClassifier(assetManager: AssetManager): ImageClassifier {
        return ImageClassifier(assetManager, LITERT_MODEL_PATH, LITERT_LABEL_PATH, INPUT_SIZE)
    }

}