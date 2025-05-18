package rs.smobile.catsvsdogs.data.network

import rs.smobile.catsvsdogs.data.network.model.CatImage
import rs.smobile.catsvsdogs.data.network.model.DogImage

/**
 * Interface representing network calls to the API
 */
interface NetworkDataSource {
    suspend fun getDogImage(): DogImage
    suspend fun getCatImage(): CatImage
}