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

    private val options = Interpreter.Options().apply {
        numThreads = NUM_OF_THREADS
        useNNAPI = true
    }
    private val cnnInterpreter = Interpreter(assetManager.loadModelFile(modelPath), options)
    private val lstmInterpreter = Interpreter(assetManager.loadModelFile(lstmModelPath), options)

    fun generateCaption(bitmap: Bitmap): String {
        val imageFeed = preprocessImage(bitmap)
        val stateFeed = runCNNInference(imageFeed)
        return generateCaptionText(stateFeed)
    }

    private fun preprocessImage(bitmap: Bitmap): Array<Array<FloatArray>> {
        val scaledBitmap = bitmap.scale(inputSize, inputSize, false)
        val imageFeed = Array(inputSize) { Array(inputSize) { FloatArray(3) } }
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = scaledBitmap[i, j]
                imageFeed[i][j][0] = ((pixelValue shr 16) and 0xFF) / IMAGE_STD
                imageFeed[i][j][1] = ((pixelValue shr 8) and 0xFF) / IMAGE_STD
                imageFeed[i][j][2] = (pixelValue and 0xFF) / IMAGE_STD
            }
        }
        return imageFeed
    }

    private fun runCNNInference(imageFeed: Array<Array<FloatArray>>): Array<FloatArray> {
        val lstmInitialState = cnnInterpreter.getOutputIndex("import/lstm/initial_state")
        val stateFeed = Array(1) { FloatArray(LSTM_STATE_SIZE) }
        //Run CNN to get image features initial state
        val outputsCnn = hashMapOf<Int, Any>(lstmInitialState to stateFeed)
        cnnInterpreter.runForMultipleInputsOutputs(arrayOf(imageFeed), outputsCnn)
        return stateFeed
    }

    private fun generateCaptionText(stateFeed: Array<FloatArray>): String {
        val softmax = Array(1) { FloatArray(VOCABULARY_SIZE) }
        val lstmState = Array(1) { FloatArray(LSTM_STATE_SIZE) }
        //Setup LSTM outputs
        val outputsLstm = hashMapOf<Int, Any>(
            lstmInterpreter.getOutputIndex("import/softmax") to softmax,
            lstmInterpreter.getOutputIndex("import/lstm/state") to lstmState
        )
        val words = mutableListOf<Int>()
        val inputFeed = Array(1) { LongArray(1) }
        repeat(MAX_CAPTION_LENGTH) {
            lstmInterpreter.runForMultipleInputsOutputs(arrayOf(inputFeed, stateFeed), outputsLstm)
            val maxId = softmax[0].findMaxId()
            if (maxId == vocabulary.getClosingTagWordIndex()) return buildCaption(words.toList())
            words.add(maxId)
            inputFeed[0][0] = maxId.toLong()
            stateFeed[0] = lstmState[0].copyOf()
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