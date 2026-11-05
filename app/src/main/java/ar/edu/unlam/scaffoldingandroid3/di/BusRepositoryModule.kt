package ar.edu.unlam.scaffoldingandroid3.di

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.BusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.BusStopDataSourceImpl
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
    fun bindStopDataSource(impl: BusStopDataSourceImpl): BusStopDataSource
}
