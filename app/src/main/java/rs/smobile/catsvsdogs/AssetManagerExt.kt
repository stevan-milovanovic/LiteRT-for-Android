package rs.smobile.catsvsdogs

import android.content.res.AssetManager
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


fun AssetManager.loadModelFile(modelPath: String): MappedByteBuffer {
    val fileDescriptor = openFd(modelPath)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}


