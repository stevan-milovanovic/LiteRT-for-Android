package rs.smobile.catsvsdogs

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import rs.smobile.catsvsdogs.captioner.ImageCaptioner
import rs.smobile.catsvsdogs.classifier.ImageClassifier
import rs.smobile.catsvsdogs.data.local.model.Image
import rs.smobile.catsvsdogs.data.repository.ImageRepository
import rs.smobile.catsvsdogs.di.CatImageRepo
import rs.smobile.catsvsdogs.di.DogImageRepo
import rs.smobile.catsvsdogs.genai.LlmDescriptor
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor(
    @DogImageRepo private val dogImageRepository: ImageRepository,
    @CatImageRepo private val catImageRepository: ImageRepository,
    private val imageClassifier: ImageClassifier,
    private val imageCaptioner: ImageCaptioner,
    private val llmInferenceModel: LlmDescriptor,
) : ViewModel() {

    private val _image = MutableStateFlow<Image?>(null)
    val image: StateFlow<Image?> = _image

    private val _expectedLabel = MutableStateFlow<String>("")
    val expectedLabel: StateFlow<String> = _expectedLabel

    private val _classifiedLabel = MutableStateFlow<String>("")
    val classifiedLabel: StateFlow<String> = _classifiedLabel

    private val _generatedCaption = MutableStateFlow<String>("")
    val generatedCaption: StateFlow<String> = _generatedCaption

    private val _generatedDescription = MutableStateFlow<String>("")
    val generatedDescription: StateFlow<String> = _generatedDescription

    private val _generatedDescriptionIsLoading = MutableStateFlow<Boolean>(false)
    val generatedDescriptionIsLoading: StateFlow<Boolean> = _generatedDescriptionIsLoading

    fun loadRandomImage() {
        viewModelScope.launch {
            _expectedLabel.value = ""
            _classifiedLabel.value = ""
            _generatedCaption.value = ""
            _generatedDescription.value = ""

            _image.value = if (Random.Default.nextBoolean()) {
                _expectedLabel.value = imageClassifier.labels.last()
                dogImageRepository.getRandomImage()
            } else {
                _expectedLabel.value = imageClassifier.labels.first()
                catImageRepository.getRandomImage()
            }
        }
    }

    fun inferImageClass(bitmap: Bitmap) {
        _classifiedLabel.value = imageClassifier.recognizeImage(bitmap).first().title
    }

    fun generateCaptionForImage(bitmap: Bitmap) {
        val generatedCaption = imageCaptioner.generateCaption(bitmap)
        _generatedCaption.value = generatedCaption
        generateImageDescription(generatedCaption)
    }

    private fun generateImageDescription(generatedCaption: String) {
        val question = "Describe image: $generatedCaption in maximum two sentences. " +
                "Skip intro as 'Here's a description and caption for the image'. " +
                "Give description right away."
        var response = ""
        _generatedDescriptionIsLoading.value = true
        llmInferenceModel.generateLlmDescription(question) { partialResult, done ->
            response += partialResult
            _generatedDescription.value = response
            _generatedDescriptionIsLoading.value = !done
        }
    }

    fun getLlmName(): String = llmInferenceModel.getModelName()

}