package ar.edu.unlam.scaffoldingandroid3.di

import android.content.Context
import ar.edu.unlam.scaffoldingandroid3.data.mapper.LocationMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LocationDataModule {
    @Provides
    fun provideFusedLocationProvideClient(
        @ApplicationContext context: Context,
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @Provides
    fun provideLocationMapper(): LocationMapper = LocationMapper()
}
