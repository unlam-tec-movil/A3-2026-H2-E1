package ar.edu.unlam.scaffoldingandroid3.di

import android.content.Context
import androidx.room.Room
import ar.edu.unlam.scaffoldingandroid3.data.database.room.AppDatabase
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.BusStopCacheDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.SearchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "voy_en_bondi.db",
            ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideBusStopCacheDao(database: AppDatabase): BusStopCacheDao = database.busStopCacheDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao = database.searchHistoryDao()
}
