package rs.smobile.catsvsdogs.data.network

import retrofit2.Response
import retrofit2.http.GET
import rs.smobile.catsvsdogs.data.network.model.DogImage

interface DogNetworkApi {
    @GET(value = "breeds/image/random")
    suspend fun getDogImage(): Response<DogImage>
}