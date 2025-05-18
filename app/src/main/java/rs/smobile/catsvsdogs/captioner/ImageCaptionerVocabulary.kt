package rs.smobile.catsvsdogs.captioner

import android.content.res.AssetManager
import java.io.InputStreamReader
import javax.inject.Inject

class ImageCaptionerVocabulary @Inject constructor(
    assetManager: AssetManager,
    vocabularyPath: String
) {

    companion object {
        const val VOCABULARY_SIZE = 12000
        private const val CLOSING_TAG_WORD_INDEX = 2 // '</S>' in vocabulary
    }

    private val words: List<String>

    init {
        words = loadWords(assetManager, vocabularyPath)
    }

    fun getClosingTagWordIndex() = CLOSING_TAG_WORD_INDEX

    fun getWordAtIndex(index: Int) = words[index]

    private fun loadWords(assetManager: AssetManager, path: String): List<String> {
        val labelList = mutableListOf<String>()
        val reader = InputStreamReader(assetManager.open(path))
        var lines = reader.readLines()
        reader.close()
        lines.forEach {
            labelList.add(it.split(" ").first())
        }

        return labelList
    }

}