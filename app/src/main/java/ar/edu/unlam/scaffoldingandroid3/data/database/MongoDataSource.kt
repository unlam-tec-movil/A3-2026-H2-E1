package ar.edu.unlam.scaffoldingandroid3.data.database

import ar.edu.unlam.scaffoldingandroid3.BuildConfig
import com.mongodb.kotlin.client.coroutine.MongoClient

/**
 * Cliente de MongoDB Atlas compartido por toda la aplicación.
 *
 * La conexión se toma de `BuildConfig.MONGODB_URI`, que se inyecta desde `local.properties`
 * en tiempo de build: las credenciales nunca viven en el código fuente ni en el repositorio.
 *
 * Si la URI no está configurada, el cliente no se crea y [getDatabase] lanza una excepción
 * que el repositorio de paradas captura para degradar la fuente (caché o demo).
 */
object MongoDataSource {
    val client: MongoClient by lazy {
        check(BuildConfig.MONGODB_URI.isNotBlank()) { MISSING_URI_MESSAGE }
        MongoClient.create(BuildConfig.MONGODB_URI)
    }

    fun getDatabase(dbName: String) = client.getDatabase(dbName)

    private const val MISSING_URI_MESSAGE =
        "Falta configurar MONGODB_URI en local.properties para conectarse a MongoDB Atlas."
}
