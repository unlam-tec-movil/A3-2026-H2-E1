package ar.edu.unlam.scaffoldingandroid3.di

import ar.edu.unlam.scaffoldingandroid3.data.datasources.remote.OsrmRouteDataSource
import ar.edu.unlam.scaffoldingandroid3.data.datasources.remote.OsrmRouteDataSourceImpl
import ar.edu.unlam.scaffoldingandroid3.data.repositories.RouteRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RouteModule {
    @Binds
    @Singleton
    fun bindOsrmRouteDataSource(impl: OsrmRouteDataSourceImpl): OsrmRouteDataSource

    @Binds
    @Singleton
    fun bindRouteRepository(impl: RouteRepositoryImpl): RouteRepository
}
