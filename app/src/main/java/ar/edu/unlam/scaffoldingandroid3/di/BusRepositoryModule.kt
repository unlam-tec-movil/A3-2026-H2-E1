package ar.edu.unlam.scaffoldingandroid3.di

import ar.edu.unlam.scaffoldingandroid3.data.datasources.demo.DemoBusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.data.datasources.demo.DemoBusStopDataSourceImpl
import ar.edu.unlam.scaffoldingandroid3.data.datasources.remote.FirestoreBusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.data.datasources.remote.RemoteBusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.data.repositories.BusStopRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.repository.BusStopRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface BusRepositoryModule {
    @Binds
    @Singleton
    fun bindRemoteBusStopDataSource(impl: FirestoreBusStopDataSource): RemoteBusStopDataSource

    @Binds
    @Singleton
    fun bindDemoBusStopDataSource(impl: DemoBusStopDataSourceImpl): DemoBusStopDataSource

    @Binds
    @Singleton
    fun bindBusStopRepository(impl: BusStopRepositoryImpl): BusStopRepository
}
