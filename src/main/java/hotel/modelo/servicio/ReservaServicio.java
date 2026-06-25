package hotel.modelo.servicio;

import hotel.modelo.entidades.Reserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservaServicio {

    Reserva buscarPorId(int id);

    List<Reserva> listar();

    Reserva crear(
            int habitacionId,
            int clienteId,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida,
            BigDecimal totalPagado
    );

    Reserva registrarPago(int reservaId, BigDecimal monto);

    Reserva cancelar(int reservaId);

    Reserva finalizar(int reservaId);

    boolean eliminar(int id);
}
