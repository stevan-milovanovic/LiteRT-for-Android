package rs.smobile.catsvsdogs.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import rs.smobile.catsvsdogs.data.network.CatNetworkApi
import rs.smobile.catsvsdogs.data.network.DogNetworkApi
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @DogApi
    fun provideDogBaseUrl(): String = "https://dog.ceo/api/"

    @Provides
    @Singleton
    fun provideDogNetworkApi(@DogApi builder: Retrofit.Builder): DogNetworkApi = builder
        .build()
        .create(DogNetworkApi::class.java)

    @Provides
    @Singleton
    @DogApi
    fun provideDogRetrofitBuilder(
        @DogApi baseUrl: String,
        client: OkHttpClient
    ): Retrofit.Builder = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())

    @Provides
    @CatApi
    fun provideCatBaseUrl(): String = "https://api.thecatapi.com/v1/"

    @Provides
    @Singleton
    fun provideCatNetworkApi(@CatApi builder: Retrofit.Builder): CatNetworkApi = builder
        .build()
        .create(CatNetworkApi::class.java)

    @Provides
    @Singleton
    @CatApi
    fun provideCatRetrofitBuilder(
        @CatApi baseUrl: String,
        client: OkHttpClient
    ): Retrofit.Builder = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor()
                .apply {
                    setLevel(HttpLoggingInterceptor.Level.BODY)
                }
        )
        .build()

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DogApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CatApi
