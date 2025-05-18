package rs.smobile.catsvsdogs.data.network.model

import com.squareup.moshi.JsonClass
import rs.smobile.catsvsdogs.data.local.model.Image

@JsonClass(generateAdapter = true)
data class DogImage(
    val message: String,
    val status: String
) {
    fun toLocal(): Image = Image(message)
}