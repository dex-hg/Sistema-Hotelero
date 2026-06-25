package hotel.modelo.servicio.impl;

import hotel.dao.ClienteDAO;
import hotel.dao.HabitacionDAO;
import hotel.dao.ReservaDAO;

import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.modelo.sesion.ProveedorHotelId;
import hotel.modelo.servicio.ReservaServicio;

import hotel.excepcion.EntidadNoEncontradaException;
import hotel.excepcion.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class ReservaServicioImpl implements ReservaServicio {

    private final ReservaDAO reservaDAO;
    private final HabitacionDAO habitacionDAO;
    private final ClienteDAO clienteDAO;
    private final ProveedorHotelId proveedorHotelId;

    public ReservaServicioImpl(
            ReservaDAO reservaDAO,
            HabitacionDAO habitacionDAO,
            ClienteDAO clienteDAO,
            ProveedorHotelId proveedorHotelId
    ) {
        this.reservaDAO = Objects.requireNonNull(reservaDAO);
        this.habitacionDAO = Objects.requireNonNull(habitacionDAO);
        this.clienteDAO = Objects.requireNonNull(clienteDAO);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
    }

    @Override
    public Reserva buscarPorId(int id) {
        return reservaDAO.buscarPorId(id).orElseThrow(
                () -> new EntidadNoEncontradaException(
                        "No existe la reserva " + id
                )
        );
    }

    @Override
    public List<Reserva> listar() {
        return reservaDAO.listar();
    }

    @Override
    public Reserva crear(
            int habitacionId,
            int clienteId,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida,
            BigDecimal totalPagado
    ) {
        if (habitacionDAO.buscarPorId(habitacionId).isEmpty()) {
            throw new ReglaNegocioException(
                    "La habitacion no pertenece al hotel activo"
            );
        }

        if (clienteDAO.buscarPorId(clienteId).isEmpty()) {
            throw new ReglaNegocioException(
                    "El cliente no pertenece al hotel activo"
            );
        }

        Reserva reserva = new Reserva(
                null, proveedorHotelId.getHotelId(),
                habitacionId, clienteId,
                fechaIngreso, fechaSalida,
                totalPagado, EstadoReserva.ACTIVA
        );

        return reservaDAO.crear(reserva);
    }

    @Override
    public Reserva registrarPago(int reservaId, BigDecimal monto) {
        Reserva reserva = exigirActiva(reservaId);
        reserva.registrarPago(monto);
        persistir(reserva);

        return reserva;
    }

    @Override
    public Reserva cancelar(int reservaId) {
        Reserva reserva = exigirActiva(reservaId);
        reserva.cambiarEstado(EstadoReserva.CANCELADA);
        persistir(reserva);

        return reserva;
    }

    @Override
    public Reserva finalizar(int reservaId) {
        Reserva reserva = exigirActiva(reservaId);
        reserva.cambiarEstado(EstadoReserva.FINALIZADA);
        persistir(reserva);

        return reserva;
    }

    @Override
    public boolean eliminar(int id) {
        return reservaDAO.eliminar(id);
    }

    private Reserva exigirActiva(int id) {
        Reserva reserva = buscarPorId(id);
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new ReglaNegocioException(
                    "La reserva debe estar ACTIVA"
            );
        }
        return reserva;
    }

    private void persistir(Reserva reserva) {
        if (!reservaDAO.actualizar(reserva)) {
            throw new EntidadNoEncontradaException(
                    "La reserva dejo de existir durante la actualizacion"
            );
        }
    }
}
