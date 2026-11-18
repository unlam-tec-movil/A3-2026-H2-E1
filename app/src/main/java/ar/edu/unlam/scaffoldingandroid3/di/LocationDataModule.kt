package ar.edu.unlam.scaffoldingandroid3.di

import android.content.Context
import ar.edu.unlam.scaffoldingandroid3.data.datasources.mapper.LocationMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * Dagger Hilt module que proporciona las dependencias relacionadas con la ubicación para el
 * componente Singleton de la aplicación.
 *
 * Este módulo se instala en el [SingletonComponent], lo que significa que las instancias
 * proporcionadas son de alcance único para toda la aplicación.
 *
 * @since 1.0
 */
@Module
@InstallIn(SingletonComponent::class)
object LocationDataModule {
    /**
     * Proporciona una instancia de [FusedLocationProviderClient] para interactuar con el
     * Fused Location Provider de Google Play Services.
     *
     * El cliente se instancia usando el contexto de la aplicación, lo que garantiza que
     * las operaciones de ubicación se ejecuten en el contexto correcto de la aplicación.
     *
     * @param context el contexto de la aplicación, utilizado para obtener el cliente de ubicación.
     * @return una instancia de [FusedLocationProviderClient] configurada para la aplicación.
     */
    @Provides
    fun provideFusedLocationProvideClient(
        @ApplicationContext context: Context,
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Proporciona una instancia de [LocationMapper] para mapear datos de ubicación entre
     * diferentes capas del sistema (datasources, repositories, etc.).
     *
     * El mapper se instancia sin dependencias externas, lo que lo hace trivial de proporcionar.
     *
     * @return una instancia de [LocationMapper] lista para ser utilizada.
     */
    @Provides
    fun provideLocationMapper(): LocationMapper = LocationMapper()
}
