package rs.smobile.catsvsdogs.captioner

import android.content.res.AssetManager
import android.graphics.Bitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import org.tensorflow.lite.Interpreter
import rs.smobile.catsvsdogs.captioner.ImageCaptionerVocabulary.Companion.VOCABULARY_SIZE
import rs.smobile.catsvsdogs.loadModelFile
import javax.inject.Inject

class ImageCaptioner @Inject constructor(
    assetManager: AssetManager,
    modelPath: String,
    lstmModelPath: String,
    private val vocabulary: ImageCaptionerVocabulary,
    private val inputSize: Int
) {
    private companion object {
        private const val MAX_CAPTION_LENGTH = 20
        private const val NUM_OF_THREADS = 4
        private const val IMAGE_STD = 255.0f
        private const val LSTM_STATE_SIZE = 1024
    }

    private var interpreter: Interpreter
    private var lstmInterpreter: Interpreter

    private var imageFeed = Array(inputSize) { Array(inputSize) { FloatArray(3) } }
    private var inputFeed = Array(1) { LongArray(1) }
    private var stateFeed = Array(1) { FloatArray(LSTM_STATE_SIZE) }

    private var softmax = Array(1) { FloatArray(VOCABULARY_SIZE) }
    private var lstmState = Array(1) { FloatArray(LSTM_STATE_SIZE) }
    private var initialState = Array(1) { FloatArray(LSTM_STATE_SIZE) }

    init {
        val options = Interpreter.Options().apply {
            numThreads = NUM_OF_THREADS
            useNNAPI = true
        }
        interpreter = Interpreter(assetManager.loadModelFile(modelPath), options)
        lstmInterpreter = Interpreter(assetManager.loadModelFile(lstmModelPath), options)
    }

    fun generateCaption(bitmap: Bitmap): String {
        preprocessImage(bitmap)
        return runInference()
    }

    private fun preprocessImage(bitmap: Bitmap) {
        val scaledBitmap = bitmap.scale(inputSize, inputSize, false)
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = scaledBitmap[i, j]
                imageFeed[i][j][0] = ((pixelValue shr 16) and 0xFF) / IMAGE_STD
                imageFeed[i][j][1] = ((pixelValue shr 8) and 0xFF) / IMAGE_STD
                imageFeed[i][j][2] = (pixelValue and 0xFF) / IMAGE_STD
            }
        }
    }

    private fun runInference(): String {
        // Run CNN to get initial state
        val outputsCnn = HashMap<Int, Any>().apply {
            put(interpreter.getOutputIndex("import/lstm/initial_state"), initialState)
        }
        interpreter.runForMultipleInputsOutputs(arrayOf(imageFeed), outputsCnn)
        System.arraycopy(initialState[0], 0, stateFeed[0], 0, initialState[0].size)

        // Setup LSTM outputs
        val outputsLstm = HashMap<Int, Any>().apply {
            put(lstmInterpreter.getOutputIndex("import/softmax"), softmax)
            put(lstmInterpreter.getOutputIndex("import/lstm/state"), lstmState)
        }

        return generateCaptionText(outputsLstm)
    }

    private fun generateCaptionText(outputsLstm: HashMap<Int, Any>): String {
        val words = mutableListOf<Int>()

        (0 until MAX_CAPTION_LENGTH).forEach { i ->
            lstmInterpreter.runForMultipleInputsOutputs(arrayOf(inputFeed, stateFeed), outputsLstm)
            val maxId = softmax[0].findMaxId()

            if (maxId == vocabulary.getClosingTagWordIndex()) {
                return buildCaption(words.toList())
            }

            words.add(maxId)

            inputFeed[0][0] = maxId.toLong()
            System.arraycopy(lstmState[0], 0, stateFeed[0], 0, stateFeed[0].size)
        }

        return buildCaption(words.toList())
    }

    private fun buildCaption(words: List<Int>): String = buildString {
        words
            .drop(1) //remove opening tag word
            .forEach { wordIndex ->
                val word = vocabulary.getWordAtIndex(wordIndex)
                if (word == "." && isNotEmpty()) {
                    deleteCharAt(length - 1) //remove blank space before a dot
                }
                append(word)
                append(" ")
            }
    }.trim()

    private fun FloatArray.findMaxId(): Int = this
        .withIndex()
        .maxByOrNull { it.value }?.index ?: 0
}