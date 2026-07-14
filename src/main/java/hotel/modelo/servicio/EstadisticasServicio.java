package hotel.modelo.servicio;

import java.time.YearMonth;

public interface EstadisticasServicio {

    ResumenDashboard obtenerDashboard();

    ResumenReportes obtenerReportes(YearMonth periodo);
}
