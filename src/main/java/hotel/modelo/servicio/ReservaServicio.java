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

    Reserva registrarRecepcion(
            String nombreCompleto,
            String documentoIdentidad,
            String telefono,
            int habitacionId,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida
    );

    Reserva registrarRecepcion(
            String nombreCompleto,
            String documentoIdentidad,
            String telefono,
            int habitacionId,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida,
            BigDecimal totalPagado
    );

    Reserva registrarPago(int reservaId, BigDecimal monto);

    Reserva registrarCheckIn(int reservaId);

    Reserva registrarCheckOut(int reservaId);

    Reserva cancelar(int reservaId);

    Reserva finalizar(int reservaId);

    boolean eliminar(int id);
}
