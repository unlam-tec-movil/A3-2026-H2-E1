
package ar.edu.unlam.scaffoldingandroid3.di

import android.content.Context
import ar.edu.unlam.scaffoldingandroid3.data.repositories.FavoriteBusStopRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.repository.FavoriteBusStopRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FavoriteBusStopModule {
    @Provides
    @Singleton
    fun provideFavoriteBusStopRepository(
        @ApplicationContext context: Context,
    ): FavoriteBusStopRepository {
        return FavoriteBusStopRepositoryImpl(context)
    }
}
