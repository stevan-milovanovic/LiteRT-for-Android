package rs.smobile.catsvsdogs.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rs.smobile.catsvsdogs.data.network.NetworkDataSource
import rs.smobile.catsvsdogs.data.repository.CatRepository
import rs.smobile.catsvsdogs.data.repository.DogRepository
import rs.smobile.catsvsdogs.data.repository.ImageRepository
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    @CatImageRepo
    fun provideCatRepository(networkDataSource: NetworkDataSource): ImageRepository =
        CatRepository(networkDataSource)

    @Provides
    @Singleton
    @DogImageRepo
    fun provideDogRepository(networkDataSource: NetworkDataSource): ImageRepository =
        DogRepository(networkDataSource)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CatImageRepo

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DogImageRepo