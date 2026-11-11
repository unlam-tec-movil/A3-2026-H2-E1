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

/**
 * Módulo de Dagger Hilt que vincula las implementaciones de datos de ubicación con sus
 * interfaces correspondientes en el componente Singleton de la aplicación.
 *
 * Este módulo utiliza la inyección de dependencias para:
 * - Vincular [LocationDataSourceImpl] como [LocationDataSource]
 * - Vincular [LocationRepositoryImpl] como [LocationRepository]
 *
 * Ambas dependencias se instancian con alcance singleton para toda la aplicación.
 *
 * @since 1.0
 */
@Module
@InstallIn(SingletonComponent::class)
interface LocationRepositoryModule {
    /**
     * Vincula la implementación [LocationDataSourceImpl] con la interfaz [LocationDataSource].
     *
     * Esta binding permite que las capas superiores de la aplicación (repositories, use cases)
     * utilicen la interfaz [LocationDataSource] mientras que la implementación real es
     * [LocationDataSourceImpl].
     *
     * @param impl la implementación de [LocationDataSource] que se debe vincular.
     * @return la misma instancia de [LocationDataSourceImpl], injectada como [LocationDataSource].
     */
    @Binds
    @Singleton
    fun bindLocationDataSource(impl: LocationDataSourceImpl): LocationDataSource

    /**
     * Vincula la implementación [LocationRepositoryImpl] con la interfaz [LocationRepository].
     *
     * Esta binding permite que las capas de dominio y presentacion utilicen la interfaz
     * [LocationRepository] mientras que la implementación real es [LocationRepositoryImpl].
     *
     * @param impl la implementación de [LocationRepository] que se debe vincular.
     * @return la misma instancia de [LocationRepositoryImpl], injectada como [LocationRepository].
     */
    @Binds
    @Singleton
    fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
}
