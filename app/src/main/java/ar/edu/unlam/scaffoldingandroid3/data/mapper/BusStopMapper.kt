package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.BusStopCacheEntity
import ar.edu.unlam.scaffoldingandroid3.domain.enums.BusLineStatus
import ar.edu.unlam.scaffoldingandroid3.domain.enums.StopStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusRoute
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val FNV_OFFSET_BASIS = 0xcbf29ce484222325uL
private const val FNV_PRIME = 0x100000001b3uL
private val DEFAULT_ROUTES = emptyList<BusRoute>()
private val DEFAULT_CURRENT_STATUS = BusLineStatus.NORMAL
private val DEFAULT_STOP_STATUS = StopStatus.ACTIVA
private const val DEFAULT_VALUE_IF_NULL_DIGIT = -1

/**
 * Object mapper para convertir entre entidades de Room y modelos de dominio de paradas de autobús.
 *
 * Proporciona funciones para transformar [BusStop] en [BusStopCacheEntity] y viceversa,
 * así como utilidades para extraer números de línea y generar hashes estables.
 *
 * @see BusStop
 * @see BusStopCacheEntity
 */
object BusStopMapper {
    /**
     * Clase interna serializable para representar líneas de autobús en caché.
     *
     * Se utiliza para serializar y deserializar líneas de autobús en el almacenamiento
     * de la entidad de parada de autobús, almacenando solo los datos esenciales.
     *
     * @property lineId El identificador único de la línea de autobús.
     * @property number El número de la línea de autobús.
     * @property code El código de la línea de autobús.
     * @property branch La rama de la línea de autobús.
     * @property destination El destino de la línea de autobús.
     * @see BusLine
     */
    @Serializable
    private data class CachedBusLine(
        val lineId: Long,
        val number: Int,
        val code: String = "",
        val branch: String = "",
        val destination: String = "",
    )

    private val jsonFormat = Json { ignoreUnknownKeys = true }

    /**
     * Convierte una entidad [BusStop] de dominio a su representación en caché [BusStopCacheEntity].
     *
     * Extrae la información de la parada y serializa las líneas de autobús asociadas
     * en una cadena JSON para almacenamiento eficiente.
     *
     * @return Una instancia de [BusStopCacheEntity] con la información de la parada y sus líneas.
     * @see BusStop
     * @see BusStopCacheEntity
     */
    fun BusStop.toCacheEntity(): BusStopCacheEntity =
        BusStopCacheEntity(
            stopId = stopId,
            name = name,
            latitude = locationData.latitude.value,
            longitude = locationData.longitude.value,
            linesJson = serializeBusLines(),
            markedAsFavorite = markedAsFavorite,
            stopStatus = status.name,
        )

    /**
     * Convierte una entidad de caché [BusStopCacheEntity] a su entidad de dominio [BusStop].
     *
     * Deserializa las líneas de autobús almacenadas en JSON y reconstruye el modelo de dominio
     * con información de ubicación, estado y líneas asociadas. Si el JSON está vacío o es inválido,
     * se manejan los valores por defecto apropiados.
     *
     * @return Una instancia de [BusStop] con la información deserializada.
     * @see BusStopCacheEntity
     * @see BusStop
     */
    fun BusStopCacheEntity.toDomain(): BusStop =
        BusStop(
            stopId = stopId,
            name = name,
            locationData =
                LocationData(
                    latitude = Latitude(latitude),
                    longitude = Longitude(longitude),
                ),
            markedAsFavorite = markedAsFavorite,
            busLines =
                if (linesJson.isBlank()) {
                    emptyList()
                } else {
                    jsonFormat.decodeFromString<List<CachedBusLine>>(linesJson).toDomainBusLines()
                },
            status =
                try {
                    StopStatus.valueOf(stopStatus)
                } catch (_: IllegalArgumentException) {
                    DEFAULT_STOP_STATUS
                },
        )

    /**
     * Extrae el número de línea de una cadena de texto.
     *
     * Intenta convertir la cadena a un entero directamente. Si falla, extrae los dígitos
     * de la cadena y los convierte a entero. Si no hay dígitos, retorna el valor por defecto.
     * Maneja excepciones de forma segura retornando el valor por defecto.
     *
     * @return El número de línea como entero, o [DEFAULT_VALUE_IF_NULL_DIGIT] si no se puede extraer.
     * @see DEFAULT_VALUE_IF_NULL_DIGIT
     */
    internal fun String.toLineNumber(): Int =
        try {
            return toIntOrNull() ?: run {
                val digits = filter { it.isDigit() }

                if (digits.isNotEmpty()) {
                    digits.toIntOrNull()
                        ?: DEFAULT_VALUE_IF_NULL_DIGIT
                } else {
                    DEFAULT_VALUE_IF_NULL_DIGIT
                }
            }
        } catch (_: Exception) {
            return DEFAULT_VALUE_IF_NULL_DIGIT
        }

    /**
     * Genera un hash estable (FNV-1a) de una cadena de texto.
     *
     * Utiliza el algoritmo FNV-1a con los constantes FNV_OFFSET_BASIS y FNV_PRIME
     * para generar un hash determinista de una cadena, útil para comparaciones y
     * almacenamiento en caché de líneas de autobús.
     *
     * @return Un hash de 64 bits (Long) basado en el algoritmo FNV-1a.
     * @see FNV_OFFSET_BASIS
     * @see FNV_PRIME
     */
    internal fun String.toStableLong(): Long = fold(FNV_OFFSET_BASIS) { acc, char -> (acc xor char.code.toULong()) * FNV_PRIME }.toLong()

    /**
     * Serializa las líneas de autobús de una parada en una cadena JSON.
     *
     * Si no hay líneas asociadas, retorna una cadena vacía. De lo contrario,
     * convierte cada línea a su representación [CachedBusLine] y las serializa
     * como una cadena JSON para almacenamiento en la entidad de caché.
     *
     * @return Una cadena JSON con las líneas de autobús serializadas, o "" si está vacío.
     * @see CachedBusLine
     */
    private fun BusStop.serializeBusLines(): String =
        if (this.busLines.isEmpty()) {
            ""
        } else {
            jsonFormat.encodeToString(
                this.busLines.map {
                    CachedBusLine(
                        lineId = it.lineId,
                        number = it.number,
                        code = it.code,
                        branch = it.branch,
                        destination = it.destination,
                    )
                },
            )
        }

    /**
     * Convierte una lista de líneas de autobús de caché [CachedBusLine] a su representación
     * de dominio [BusLine].
     *
     * Mapea cada línea de la lista serializada a su entidad de dominio, aplicando
     * valores por defecto para rutas y estado actual, y usando el número como código
     * si el código está vacío.
     *
     * @return Una lista de instancias de [BusLine] con la información procesada.
     * @see CachedBusLine
     * @see BusLine
     */
    private fun List<CachedBusLine>.toDomainBusLines(): List<BusLine> =
        this.map { cached ->
            BusLine(
                lineId = cached.lineId,
                number = cached.number,
                routes = DEFAULT_ROUTES,
                currentStatus = DEFAULT_CURRENT_STATUS,
                code = cached.code.ifBlank { cached.number.toString() },
                branch = cached.branch,
                destination = cached.destination,
            )
        }
}
