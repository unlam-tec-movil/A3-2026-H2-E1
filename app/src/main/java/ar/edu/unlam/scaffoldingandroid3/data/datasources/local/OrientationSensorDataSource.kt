package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import kotlinx.coroutines.flow.StateFlow

/**
 * Interfaz de fuente de datos para el sensor de orientación del dispositivo.
 *
 * Esta interfaz define el contrato para acceder a los datos de orientación del dispositivo,
 * proporcionando un flujo de estado para los grados de orientación y métodos para controlar
 * la escucha de datos de orientación.
 *
 * **Funcionalidad**:
 * - Proporciona un flujo de estado ([StateFlow]) con los grados de orientación del dispositivo
 * - Permite verificar la disponibilidad del sensor de orientación
 * - Controla el inicio y finalización de la escucha de datos de orientación
 *
 * **Uso**:
 * - Los implementadores deben exponer el estado de orientación a través de [orientationDegrees]
 * - [startListening()] debe iniciar la escucha de datos de orientación en tiempo real
 * - [stopListening()] debe detener la escucha para liberar recursos
 *
 * @see StateFlow
 */
interface OrientationSensorDataSource {
    /**
     * Flujo de estado con los grados de orientación del dispositivo.
     *
     * Este valor representa la orientación actual del dispositivo en grados,
     * típicamente en el rango de -180 a 180 grados, donde:
     * - 0 grados indica que la parte superior del dispositivo apunta al norte
     * - Valores positivos indican rotación hacia el este
     * - Valores negativos indican rotación hacia el oeste
     *
     * El valor puede ser null cuando no hay datos de orientación disponibles.
     *
     * @see StateFlow
     */
    val orientationDegrees: StateFlow<Float?>

    /**
     * Verifica si el sensor de orientación está disponible en el dispositivo.
     *
     * Retorna true si el hardware del sensor de orientación está disponible y
     * el sistema lo reconoce, false en caso contrario.
     *
     * @return true si el sensor de orientación está disponible, false si no lo está.
     */
    fun isOrientationSensorAvailable(): Boolean

    /**
     * Inicia la escucha de datos de orientación del dispositivo.
     *
     * Este método debe ser llamado para comenzar a recibir actualizaciones en tiempo
     * real de los grados de orientación a través del flujo [orientationDegrees].
     * La escucha continuará hasta que se llame a [stopListening()].
     *
     * **Comportamiento**:
     * - Inicia la escucha de datos de orientación del sensor
     * - Los nuevos valores de orientación se emiten a través de [orientationDegrees]
     * - Debe ser llamado antes de esperar actualizaciones de orientación
     *
     * @see orientationDegrees
     */
    fun startListening()

    /**
     * Detiene la escucha de datos de orientación del dispositivo.
     *
     * Este método debe ser llamado para detener la escucha activa de datos de
     * orientación y liberar los recursos asociados.
     *
     * **Comportamiento**:
     * - Detiene la escucha de datos de orientación del sensor
     * - Libera recursos asociados con la escucha activa
     * - Debe ser llamado cuando ya no se necesiten actualizaciones de orientación
     *
     * @see orientationDegrees
     */
    fun stopListening()
}
