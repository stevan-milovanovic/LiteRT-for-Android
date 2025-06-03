package rs.smobile.catsvsdogs.classifier

import android.content.res.AssetManager
import android.graphics.Bitmap
import androidx.core.graphics.scale
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.nnapi.NnApiDelegateImpl
import rs.smobile.catsvsdogs.loadModelFile
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect
import java.nio.ByteOrder.nativeOrder
import java.util.PriorityQueue
import javax.inject.Inject
import kotlin.math.min

class ImageClassifier @Inject constructor(
    assetManager: AssetManager,
    modelPath: String,
    labelPath: String,
    private val inputSize: Int
) {

    private companion object {
        private const val NUM_OF_THREADS = 5
        private const val MAX_NUM_OF_RESULTS = 3
        private const val BYTES_PER_FLOAT_VALUE = 4
        private const val NUM_OF_RGB_COLOR_CHANNELS = 3

        private const val IMAGE_MEAN = 0f
        private const val IMAGE_STD = 255.0f
        private const val CONFIDENCE_THRESHOLD = 0.4f

        /**
         * There is only one tensor with image classification classes scores
         */
        private const val OUTPUT_TENSORS_COUNT = 1
    }

    private val interpreterApi: InterpreterApi
    val labels: List<String>

    init {
        val options = Interpreter.Options().apply {
            numThreads = NUM_OF_THREADS
            useNNAPI = true
            val nnApiDelegate = NnApiDelegate.Options().apply { useNnapiCpu = true }
            addDelegate(NnApiDelegateImpl(nnApiDelegate))
        }
        interpreterApi = InterpreterApi.create(assetManager.loadModelFile(modelPath), options)
        labels = loadLabels(assetManager, labelPath)
    }

    /**
     * Returns the result after running the recognition with the help of interpreter
     * on the passed bitmap
     */
    fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        val scaledBitmap = bitmap.scale(inputSize, inputSize, false)
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        val result = Array(OUTPUT_TENSORS_COUNT) { FloatArray(labels.size) }
        interpreterApi.run(byteBuffer, result)
        return getSortedResult(result)
    }

    /**
     * Batch size for float in Java/Kotlin is 4 bytes.
     * We're putting RGB values as floats, so we need 4 bytes per value.
     * Number of color channels — 3 for RGB, 1 for grayscale.
     */
    private fun convertBitmapToByteBuffer(
        bitmap: Bitmap,
        bytesPerValue: Int = BYTES_PER_FLOAT_VALUE,
        numOfColorChannels: Int = NUM_OF_RGB_COLOR_CHANNELS
    ): ByteBuffer {
        val byteBuffer = allocateDirect(bytesPerValue * inputSize * inputSize * numOfColorChannels)
        byteBuffer.order(nativeOrder())
        val intValues = IntArray(inputSize * inputSize)

        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        (0 until inputSize).forEach {
            (0 until inputSize).forEach {
                val input = intValues[pixel++]

                val red = (input shr 16 and 0xFF) - IMAGE_MEAN
                val green = (input shr 8 and 0xFF) - IMAGE_MEAN
                val blue = (input and 0xFF) - IMAGE_MEAN

                byteBuffer.putFloat(red / IMAGE_STD)
                byteBuffer.putFloat(green / IMAGE_STD)
                byteBuffer.putFloat(blue / IMAGE_STD)
            }
        }
        return byteBuffer
    }

    private fun getSortedResult(probabilities: Array<FloatArray>): List<Recognition> {
        val pq = PriorityQueue(
            MAX_NUM_OF_RESULTS,
            Comparator<Recognition> { (_, _, confidence1), (_, _, confidence2) ->
                confidence1.compareTo(confidence2) * -1
            })

        for (i in labels.indices) {
            val confidence = probabilities[0][i]
            if (confidence >= CONFIDENCE_THRESHOLD) {
                pq.add(Recognition(i.toString(), labels[i], confidence))
            }
        }

        val recognitions = arrayListOf<Recognition>()
        val recognitionsSize = min(pq.size, MAX_NUM_OF_RESULTS)
        (0 until recognitionsSize).forEach { i ->
            pq.poll()?.let {
                recognitions.add(it)
            }
        }
        return recognitions
    }

    private fun loadLabels(
        assetManager: AssetManager,
        labelPath: String,
    ): List<String> = assetManager.open(labelPath).bufferedReader().useLines { it.toList() }

}