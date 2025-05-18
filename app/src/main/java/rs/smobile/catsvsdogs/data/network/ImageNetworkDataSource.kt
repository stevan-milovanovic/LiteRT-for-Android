package rs.smobile.catsvsdogs.data.network

import android.util.Log
import rs.smobile.catsvsdogs.data.network.model.CatImage
import rs.smobile.catsvsdogs.data.network.model.DogImage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [Retrofit] backed [NetworkDataSource]
 */
@Singleton
class ImageNetworkDataSource @Inject constructor(
    private val dogNetworkApi: DogNetworkApi,
    private val catNetworkApi: CatNetworkApi,
) : NetworkDataSource {

    companion object {
        private const val TAG = "Network Layer"
    }

    override suspend fun getDogImage(): DogImage {
        return try {
            val response = dogNetworkApi.getDogImage()
            if (response.isSuccessful) {
                return response.body() ?: throw IllegalStateException(response.toString())
            }
            throw IllegalStateException(response.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception while trying to fetch dog image", e)
            throw e
        }
    }

    override suspend fun getCatImage(): CatImage {
        return try {
            val response = catNetworkApi.getCatImage()
            if (response.isSuccessful) {
                return response.body()?.firstOrNull() ?: throw IllegalStateException(response.toString())
            }
            throw IllegalStateException(response.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception while trying to fetch cat image", e)
            throw e
        }
    }

}