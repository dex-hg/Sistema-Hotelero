package hotel.modelo.servicio.impl;

import hotel.conexion.EjecutorTransaccional;
import hotel.conexion.OperacionTransaccional;
import hotel.dao.ClienteDAO;
import hotel.dao.HabitacionDAO;
import hotel.dao.ReservaDAO;

import hotel.modelo.entidades.Cliente;
import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.modelo.sesion.ProveedorHotelId;
import hotel.modelo.servicio.ReservaServicio;

import hotel.excepcion.EntidadNoEncontradaException;
import hotel.excepcion.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public final class ReservaServicioImpl implements ReservaServicio {

    private final ReservaDAO reservaDAO;
    private final HabitacionDAO habitacionDAO;
    private final ClienteDAO clienteDAO;
    private final ProveedorHotelId proveedorHotelId;
    private final EjecutorTransaccional ejecutorTransaccional;

    public ReservaServicioImpl(
            ReservaDAO reservaDAO,
            HabitacionDAO habitacionDAO,
            ClienteDAO clienteDAO,
            ProveedorHotelId proveedorHotelId
    ) {
        this(
                reservaDAO,
                habitacionDAO,
                clienteDAO,
                proveedorHotelId,
                new EjecutorTransaccional() {
                    @Override
                    public <T> T ejecutar(OperacionTransaccional<T> operacion) {
                        return operacion.ejecutar();
                    }
                }
        );
    }

    public ReservaServicioImpl(
            ReservaDAO reservaDAO,
            HabitacionDAO habitacionDAO,
            ClienteDAO clienteDAO,
            ProveedorHotelId proveedorHotelId,
            EjecutorTransaccional ejecutorTransaccional
    ) {
        this.reservaDAO = Objects.requireNonNull(reservaDAO);
        this.habitacionDAO = Objects.requireNonNull(habitacionDAO);
        this.clienteDAO = Objects.requireNonNull(clienteDAO);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
        this.ejecutorTransaccional = Objects.requireNonNull(ejecutorTransaccional);
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
    public Reserva registrarRecepcion(
            String nombreCompleto,
            String documentoIdentidad,
            String telefono,
            int habitacionId,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida
    ) {
        Habitacion habitacion = habitacionDAO.buscarPorId(habitacionId)
                .orElseThrow(() -> new ReglaNegocioException(
                "La habitacion no pertenece al hotel activo"
        ));

        return registrarRecepcion(
                nombreCompleto,
                documentoIdentidad,
                telefono,
                habitacionId,
                fechaIngreso,
                fechaSalida,
                calcularTotalHospedaje(habitacion, fechaIngreso, fechaSalida)
        );
    }

    @Override
    public Reserva registrarRecepcion(
            String nombreCompleto,
            String documentoIdentidad,
            String telefono,
            int habitacionId,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida,
            BigDecimal totalPagado
    ) {
        return ejecutorTransaccional.ejecutar(() -> {
            Habitacion habitacion = habitacionDAO.buscarPorId(habitacionId)
                    .orElseThrow(() -> new ReglaNegocioException(
                    "La habitacion no pertenece al hotel activo"
            ));

            Cliente cliente = guardarOActualizarCliente(
                    nombreCompleto,
                    documentoIdentidad,
                    telefono
            );

            Reserva reserva = reservaDAO.crear(new Reserva(
                    null,
                    proveedorHotelId.getHotelId(),
                    habitacion.getId(),
                    cliente.getId(),
                    fechaIngreso,
                    fechaSalida,
                    totalPagado,
                    EstadoReserva.ACTIVA
            ));

            habitacion.ocupar();
            persistir(habitacion);

            return reserva;
        });
    }

    private BigDecimal calcularTotalHospedaje(
            Habitacion habitacion,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida
    ) {
        long dias = ChronoUnit.DAYS.between(
                fechaIngreso.toLocalDate(),
                fechaSalida.toLocalDate()
        );
        long diasFacturables = Math.max(1, dias);

        return habitacion.getPrecioPorNoche().multiply(
                BigDecimal.valueOf(diasFacturables)
        );
    }

    @Override
    public Reserva registrarPago(int reservaId, BigDecimal monto) {
        Reserva reserva = exigirActiva(reservaId);
        reserva.registrarPago(monto);
        persistir(reserva);

        return reserva;
    }

    @Override
    public Reserva registrarCheckIn(int reservaId) {
        return ejecutorTransaccional.ejecutar(() -> {
            Reserva reserva = exigirActiva(reservaId);
            Habitacion habitacion = buscarHabitacionDeReserva(reserva);

            habitacion.ocupar();
            persistir(habitacion);

            return reserva;
        });
    }

    @Override
    public Reserva registrarCheckOut(int reservaId) {
        return ejecutorTransaccional.ejecutar(() -> {
            Reserva reserva = exigirActiva(reservaId);
            Habitacion habitacion = buscarHabitacionDeReserva(reserva);

            habitacion.iniciarLimpieza();
            reserva.cambiarEstado(EstadoReserva.FINALIZADA);

            persistir(habitacion);
            persistir(reserva);

            return reserva;
        });
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

    private Cliente guardarOActualizarCliente(
            String nombreCompleto,
            String documentoIdentidad,
            String telefono
    ) {
        return clienteDAO.buscarPorDocumento(documentoIdentidad)
                .map(cliente -> actualizarCliente(
                cliente,
                nombreCompleto,
                documentoIdentidad,
                telefono
        ))
                .orElseGet(() -> clienteDAO.crear(new Cliente(
                null,
                proveedorHotelId.getHotelId(),
                nombreCompleto,
                documentoIdentidad,
                telefono
        )));
    }

    private Cliente actualizarCliente(
            Cliente cliente,
            String nombreCompleto,
            String documentoIdentidad,
            String telefono
    ) {
        Cliente actualizado = new Cliente(
                cliente.getId(),
                proveedorHotelId.getHotelId(),
                nombreCompleto,
                documentoIdentidad,
                telefono
        );

        if (!clienteDAO.actualizar(actualizado)) {
            throw new EntidadNoEncontradaException(
                    "El cliente dejo de existir durante la actualizacion"
            );
        }

        return actualizado;
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

    private Habitacion buscarHabitacionDeReserva(Reserva reserva) {
        return habitacionDAO.buscarPorId(reserva.getHabitacionId()).orElseThrow(
                () -> new EntidadNoEncontradaException(
                        "No existe la habitacion de la reserva " + reserva.getId()
                )
        );
    }

    private void persistir(Reserva reserva) {
        if (!reservaDAO.actualizar(reserva)) {
            throw new EntidadNoEncontradaException(
                    "La reserva dejo de existir durante la actualizacion"
            );
        }
    }

    private void persistir(Habitacion habitacion) {
        if (!habitacionDAO.actualizar(habitacion)) {
            throw new EntidadNoEncontradaException(
                    "La habitacion dejo de existir durante la actualizacion"
            );
        }
    }
}
