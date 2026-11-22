package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toLineNumber
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toStableLong
import ar.edu.unlam.scaffoldingandroid3.data.mapper.FirestoreBusStopMapper.extractBranchFromCode
import ar.edu.unlam.scaffoldingandroid3.data.mapper.FirestoreBusStopMapper.extractFinalBranch
import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusLine
import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusStop
import ar.edu.unlam.scaffoldingandroid3.domain.enums.BusLineStatus
import ar.edu.unlam.scaffoldingandroid3.domain.enums.StopStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusRoute
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude

private const val FALLBACK_STOP_NAME = "Parada de Colectivo"
private const val DEFAULT_MARKED_AS_FAVORITE = false
private val DEFAULT_STATUS = StopStatus.ACTIVA
private val DEFAULT_CURRENT_STATUS = BusLineStatus.NORMAL
private val DEFAULT_ROUTES = emptyList<BusRoute>()

/**
 * Object mapper para convertir modelos de Firestore a dominios.
 *
 * Proporciona funciones para transformar [FirestoreBusStop] en [BusStop] y
 * [FirestoreBusLine] en [BusLine], aplicando lógica de extracción de rama,
 * normalización de códigos de línea y manejo de valores por defecto.
 *
 * @see BusStop
 * @see BusLine
 * @see FirestoreBusStop
 * @see FirestoreBusLine
 */
object FirestoreBusStopMapper {
    /**
     * Convierte un modelo [FirestoreBusStop] de Firestore a su entidad de dominio [BusStop].
     *
     * Extrae la ubicación del documento, asigna un nombre por defecto si el nombre está vacío,
     * y construye una lista de líneas de autobús asociadas eliminando duplicados por [BusLine.lineId].
     * Si el campo [location] es nulo, retorna null.
     *
     * @param documentId El identificador único del documento en Firestore, convertido a [BusStop].
     * @return Una instancia de [BusStop] si tiene ubicación, o null si [location] es nulo.
     * @see BusStop
     * @see FirestoreBusStop
     */
    fun FirestoreBusStop.toDomain(documentId: String): BusStop? {
        val loc = location ?: return null

        return BusStop(
            stopId = documentId.toStableLong(),
            name = name.ifBlank { FALLBACK_STOP_NAME },
            locationData =
                LocationData(
                    latitude = Latitude(loc.latitude),
                    longitude = Longitude(loc.longitude),
                ),
            markedAsFavorite = DEFAULT_MARKED_AS_FAVORITE,
            busLines = lines.map { it.toDomain() }.distinctBy { it.lineId },
            status = DEFAULT_STATUS,
        )
    }

    /**
     * Convierte un modelo [FirestoreBusLine] de Firestore a su entidad de dominio [BusLine].
     *
     * Procesa el código de línea para extraer el número, rama y destino, aplicando
     * transformaciones como:
     * - Eliminar espacios en blanco del código
     * - Convertir "I" a "Ida" y "V" a "Vuelta" en el destino
     * - Extraer la rama final de `ramal`, `branch` o el código residual
     *
     * @return Una instancia de [BusLine] con la información procesada.
     * @see BusLine
     * @see FirestoreBusLine
     */
    fun FirestoreBusLine.toDomain(): BusLine {
        val lineCode = code.trim().ifBlank { "-" }
        val lineNumber = lineCode.toLineNumber()
        val digits = lineNumber.toString()
        val extractedBranch = extractBranchFromCode(lineCode, digits)
        val finalBranch =
            extractFinalBranch(extractedBranch)

        val dest = destination?.trim() ?: ""
        val formattedDestination =
            when (dest.uppercase()) {
                "I" -> "Ida"
                "V" -> "Vuelta"
                else -> dest.ifBlank { "" }
            }

        val stableLineKey = "$lineCode-$finalBranch-$formattedDestination"

        return BusLine(
            lineId = stableLineKey.toStableLong(),
            number = lineNumber,
            routes = DEFAULT_ROUTES,
            currentStatus = DEFAULT_CURRENT_STATUS,
            code = lineCode,
            branch = finalBranch,
            destination = formattedDestination,
        )
    }

    /**
     * Extrae la rama intermedia del código de línea.
     *
     * Solo extrae la rama si el código comienza con los dígitos del número de línea
     * y tiene longitud mayor a la de los dígitos.
     *
     * @param lineCode El código completo de la línea de autobús.
     * @param digits Los dígitos del número de línea extraídos previamente.
     * @return La rama intermedia extraída, o una cadena vacía si no hay rama.
     * @see extractFinalBranch
     */
    private fun extractBranchFromCode(
        lineCode: String,
        digits: String,
    ): String =
        if (lineCode.startsWith(digits) && lineCode.length > digits.length) {
            lineCode.removePrefix(digits).trim()
        } else {
            ""
        }

    /**
     * Extrae la rama final de la línea de autobús.
     *
     * Prioriza los valores en este orden:
     * 1. `ramal` (si no está vacío)
     * 2. `branch` (si no está vacío)
     * 3. `extractedBranch` (como fallback)
     *
     * @param extractedBranch La rama extraída del código residual.
     * @return La rama final no nula, o una cadena vacía si todas son nulas.
     * @see extractBranchFromCode
     */
    private fun FirestoreBusLine.extractFinalBranch(extractedBranch: String): String =
        (
            ramal?.trim()?.ifBlank { null }
                ?: branch?.trim()?.ifBlank { null }
                ?: extractedBranch
        )
}
