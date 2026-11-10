package ar.edu.unlam.scaffoldingandroid3.di

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.OrientationSensorDataSource
import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.OrientationSensorDataSourceImpl
import ar.edu.unlam.scaffoldingandroid3.data.repositories.SensorRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SensorRepositoryModule {
    @Binds
    @Singleton
    fun bindOrientationSensorDataSource(impl: OrientationSensorDataSourceImpl): OrientationSensorDataSource

    @Binds
    @Singleton
    fun bindSensorRepository(impl: SensorRepositoryImpl): SensorRepository
}
