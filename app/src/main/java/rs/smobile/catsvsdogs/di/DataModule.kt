package rs.smobile.catsvsdogs.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rs.smobile.catsvsdogs.data.network.ImageNetworkDataSource
import rs.smobile.catsvsdogs.data.network.NetworkDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Singleton
    @Binds
    abstract fun bindNetworkDataSource(dataSource: ImageNetworkDataSource): NetworkDataSource
}
