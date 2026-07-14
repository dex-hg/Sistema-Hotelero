package hotel.modelo.servicio;

import hotel.modelo.entidades.constantes.EstadoReserva;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Indicadores agregados para la vista de reportes.
 */
public record ResumenReportes(
        String habitacionMasReservada,
        double diasPromedioEstadia,
        BigDecimal valorReservasMes,
        int cantidadClientes,
        int huespedesRecurrentes,
        Map<EstadoReserva, Integer> reservasPorEstado
) {

    public ResumenReportes {
        reservasPorEstado = Map.copyOf(reservasPorEstado);
    }
}
