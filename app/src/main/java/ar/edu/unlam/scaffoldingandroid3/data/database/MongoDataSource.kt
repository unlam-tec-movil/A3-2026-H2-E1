package ar.edu.unlam.scaffoldingandroid3.data.database

import com.mongodb.kotlin.client.coroutine.MongoClient

object MongoDataSource {
    private const val URI =
        "mongodb://paezfederico96_db_user:LD69MJEH0L4Q9NBN@" +
            "ac-qcmp5ot-shard-00-00.xayhiab.mongodb.net:27017," +
            "ac-qcmp5ot-shard-00-01.xayhiab.mongodb.net:27017," +
            "ac-qcmp5ot-shard-00-02.xayhiab.mongodb.net:27017/" +
            "?tls=true&authSource=admin&replicaSet=atlas-iq7bku-shard-0" +
            "&serverSelectionTimeoutMS=5000&connectTimeoutMS=5000"
    val client: MongoClient by lazy {
        MongoClient.create(URI)
    }

    fun getDatabase(dbName: String) = client.getDatabase(dbName)
}
