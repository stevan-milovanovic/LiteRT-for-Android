package rs.smobile.catsvsdogs.data.network.model

import com.squareup.moshi.JsonClass
import rs.smobile.catsvsdogs.data.local.model.Image


@JsonClass(generateAdapter = true)
data class CatImage(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int
) {
    fun toLocal(): Image = Image(url)
}