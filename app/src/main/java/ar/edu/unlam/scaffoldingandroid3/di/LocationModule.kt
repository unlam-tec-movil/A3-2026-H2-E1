package ar.edu.unlam.scaffoldingandroid3.di

import android.content.Context
import ar.edu.unlam.scaffoldingandroid3.data.LocationRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
    @Provides
    @Singleton
    fun provideLocationRepo(
        @ApplicationContext context: Context,
    ): LocationRepo = LocationRepo(context)
}
