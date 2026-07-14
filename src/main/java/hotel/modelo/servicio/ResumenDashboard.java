package hotel.modelo.servicio;

/**
 * Datos ya calculados que necesita la pantalla principal.
 */
public record ResumenDashboard(
        int habitacionesDisponibles,
        int habitacionesOcupadas,
        int habitacionesEnLimpieza,
        int reservasActivas,
        int cantidadClientes
) {
}
