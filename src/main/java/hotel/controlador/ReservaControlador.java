package hotel.controlador;

import hotel.modelo.entidades.Reserva;
import hotel.modelo.servicio.ReservaServicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Objects;

public final class ReservaControlador {

    private final ReservaServicio servicio;

    public ReservaControlador(ReservaServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio);
    }

    public List<Reserva> listar() {
        return servicio.listar();
    }

    public Reserva buscarPorId(int id) {
        return servicio.buscarPorId(id);
    }

    public Reserva crear(
            int habitacionId,
            int clienteId,
            LocalDateTime ingreso,
            LocalDateTime salida,
            BigDecimal totalPagado
    ) {
        return servicio.crear(
                habitacionId,
                clienteId,
                ingreso,
                salida,
                totalPagado
        );
    }

    public Reserva registrarPago(int id, BigDecimal monto) {
        return servicio.registrarPago(id, monto);
    }

    public Reserva cancelar(int id) {
        return servicio.cancelar(id);
    }

    public Reserva finalizar(int id) {
        return servicio.finalizar(id);
    }

    public boolean eliminar(int id) {
        return servicio.eliminar(id);
    }
}
