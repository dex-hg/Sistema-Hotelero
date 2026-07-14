package hotel.controlador;

import hotel.modelo.servicio.EstadisticasServicio;
import hotel.modelo.servicio.ResumenDashboard;
import hotel.modelo.servicio.ResumenReportes;

import java.time.YearMonth;
import java.util.Objects;

public final class EstadisticasControlador {

    private final EstadisticasServicio servicio;

    public EstadisticasControlador(EstadisticasServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio);
    }

    public ResumenDashboard obtenerDashboard() {
        return servicio.obtenerDashboard();
    }

    public ResumenReportes obtenerReportesActuales() {
        return servicio.obtenerReportes(YearMonth.now());
    }
}
