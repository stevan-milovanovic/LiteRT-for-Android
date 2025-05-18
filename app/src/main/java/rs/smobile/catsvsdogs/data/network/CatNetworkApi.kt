package rs.smobile.catsvsdogs.data.network

import retrofit2.Response
import retrofit2.http.GET
import rs.smobile.catsvsdogs.data.network.model.CatImage

interface CatNetworkApi {
    @GET(value = "images/search")
    suspend fun getCatImage(): Response<List<CatImage>>
}