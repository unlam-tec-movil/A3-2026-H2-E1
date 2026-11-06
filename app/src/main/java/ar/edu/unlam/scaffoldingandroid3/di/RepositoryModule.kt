package ar.edu.unlam.scaffoldingandroid3.di

import ar.edu.unlam.scaffoldingandroid3.data.BusStopRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.BusStopRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindBusStopRepository(busStopRepositoryImpl: BusStopRepositoryImpl): BusStopRepository
}
