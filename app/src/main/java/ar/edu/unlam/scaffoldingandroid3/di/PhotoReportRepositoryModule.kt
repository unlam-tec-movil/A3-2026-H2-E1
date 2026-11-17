package ar.edu.unlam.scaffoldingandroid3.di

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSource
import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSourceImpl.PhotoReportDataSourceImpl
import ar.edu.unlam.scaffoldingandroid3.data.repositories.PhotoReportRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PhotoReportRepositoryModule {
    @Binds
    @Singleton
    fun bindPhotoRepositoryDataSource(impl: PhotoReportDataSourceImpl): PhotoReportDataSource

    @Binds
    @Singleton
    fun bindPhotoReportRepository(impl: PhotoReportRepositoryImpl): PhotoReportRepository
}
