package ar.edu.unlam.scaffoldingandroid3.data.di

import ar.edu.unlam.scaffoldingandroid3.BuildConfig
import ar.edu.unlam.scaffoldingandroid3.data.mongodb.BusStopDocument
import ar.edu.unlam.scaffoldingandroid3.data.mongodb.BusStopMongoRepo
import ar.edu.unlam.scaffoldingandroid3.domain.BusStopRepository
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindBusStopRepository(repo: BusStopMongoRepo): BusStopRepository

    companion object {
        @Provides
        @Singleton
        fun provideMongoClient(): MongoClient = MongoClient.create(BuildConfig.MONGODB_URI)

        @Provides
        @Singleton
        fun provideBusStopCollection(client: MongoClient): MongoCollection<BusStopDocument> =
            client
                .getDatabase(BuildConfig.MONGODB_DATABASE)
                .getCollection<BusStopDocument>(BuildConfig.MONGODB_COLLECTION)
    }
}
