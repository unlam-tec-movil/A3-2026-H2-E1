package ar.edu.unlam.scaffoldingandroid3.di

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.LocationDataSource
import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.LocationDataSourceImpl
import ar.edu.unlam.scaffoldingandroid3.data.repositories.LocationRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface LocationRepositoryModule {
    @Binds
    @Singleton
    fun bindLocationDataSource(impl: LocationDataSourceImpl): LocationDataSource

    @Binds
    @Singleton
    fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
}
