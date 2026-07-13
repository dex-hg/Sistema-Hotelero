package hotel.modelo.servicio.impl;

import hotel.conexion.EjecutorTransaccional;

import hotel.dao.ClienteDAO;
import hotel.dao.HabitacionDAO;
import hotel.dao.ReservaDAO;

import hotel.modelo.entidades.Cliente;
import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.modelo.seguridad.AutorizadorAcceso;
import hotel.modelo.sesion.ProveedorHotelId;
import hotel.modelo.servicio.DatosHuespedRecepcion;
import hotel.modelo.servicio.ReservaServicio;

import hotel.excepcion.EntidadNoEncontradaException;
import hotel.excepcion.ReglaNegocioException;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

/**
 * Implementación de {@link ReservaServicio} que coordina las reglas de negocio
 * y operaciones del ciclo de vida de las reservas (creación, check-in,
 * check-out).
 *
 * APLICA PRINCIPIO SOLID: - SRP (Single Responsibility Principle): Esta clase
 * tiene la única responsabilidad de orquestar la lógica de negocio de las
 * reservas de hotel. No se preocupa por el acceso directo a la base de datos
 * (delegado a los DAOs) ni por la gestión directa de conexiones o transacciones
 * físicas.
 */
public final class ReservaServicioImpl implements ReservaServicio {

    private final ReservaDAO reservaDAO;
    private final HabitacionDAO habitacionDAO;
    private final ClienteDAO clienteDAO;
    private final ProveedorHotelId proveedorHotelId;
    private final EjecutorTransaccional ejecutorTransaccional;
    private final AutorizadorAcceso autorizadorAcceso;

    /**
     * Constructor para inyección de dependencias completa.
     *
     * APLICA PRINCIPIO SOLID: - DIP (Dependency Inversion Principle): Depende
     * exclusivamente de interfaces (abstracciones como {@link ReservaDAO},
     * {@link HabitacionDAO},
     * {@link ClienteDAO},
     * {@link ProveedorHotelId} y {@link EjecutorTransaccional}) en lugar de
     * implementaciones concretas. Esto facilita la extensibilidad y
     * testeabilidad del servicio.
     */
    public ReservaServicioImpl(
            ReservaDAO reservaDAO,
            HabitacionDAO habitacionDAO,
            ClienteDAO clienteDAO,
            ProveedorHotelId proveedorHotelId,
            EjecutorTransaccional ejecutorTransaccional,
            AutorizadorAcceso autorizadorAcceso
    ) {
        this.reservaDAO = Objects.requireNonNull(reservaDAO);
        this.habitacionDAO = Objects.requireNonNull(habitacionDAO);
        this.clienteDAO = Objects.requireNonNull(clienteDAO);

        this.proveedorHotelId = Objects.requireNonNull(
                proveedorHotelId
        );
        this.ejecutorTransaccional = Objects.requireNonNull(
                ejecutorTransaccional
        );
        this.autorizadorAcceso = Objects.requireNonNull(autorizadorAcceso);
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
    public int finalizarVencidas(LocalDateTime fechaActual) {
        Objects.requireNonNull(fechaActual, "fechaActual es obligatoria");
        return ejecutorTransaccional.ejecutar(() -> {
            int finalizadas = 0;
            for (Reserva reserva : reservaDAO.listar()) {
                if (reserva.getEstado() != EstadoReserva.ACTIVA
                        || reserva.getFechaSalida().isAfter(fechaActual)) {
                    continue;
                }

                reserva.cambiarEstado(EstadoReserva.FINALIZADA);
                persistir(reserva);

                Habitacion habitacion = buscarHabitacionDeReserva(reserva);
                if (habitacion.getEstado() == EstadoHabitacion.OCUPADA) {
                    habitacion.iniciarLimpieza();
                    persistir(habitacion);
                }
                finalizadas++;
            }
            return finalizadas;
        });
    }

    @Override
    public List<Cliente> listarHuespedes(int reservaId) {
        Reserva reserva = buscarPorId(reservaId);
        List<Cliente> huespedes = reservaDAO.listarHuespedes(reservaId);
        if (!huespedes.isEmpty()) {
            return huespedes;
        }

        Cliente principal = clienteDAO.buscarPorId(reserva.getClienteId())
                .orElseThrow(() -> new EntidadNoEncontradaException(
                "No existe el huésped principal de la reserva "
                + reserva.getId()
        ));
        return List.of(principal);
    }

    @Override
    public Map<Integer, List<Cliente>> listarHuespedesPorReserva() {
        return reservaDAO.listarHuespedesPorReserva();
    }

    @Override
    public Reserva crear(
            int habitacionId,
            int clienteId,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida
    ) {
        autorizadorAcceso.exigirAdministrador();
        return ejecutorTransaccional.ejecutar(() -> {
            Habitacion habitacion
                    = exigirHabitacionDisponibleParaActualizar(habitacionId);
            exigirDisponibilidadEnFechas(
                    habitacionId,
                    fechaIngreso,
                    fechaSalida
            );

            exigirHuespedDisponible(clienteId);

            Reserva creada = reservaDAO.crear(new Reserva(
                    null,
                    proveedorHotelId.getHotelId(),
                    habitacionId,
                    clienteId,
                    fechaIngreso,
                    fechaSalida,
                    calcularTotalHospedaje(
                            habitacion.getPrecioPorNoche(),
                            fechaIngreso,
                            fechaSalida
                    ),
                    BigDecimal.ZERO,
                    EstadoReserva.ACTIVA
            ));
            reservaDAO.asociarHuesped(creada.getId(), clienteId, true);
            return creada;
        });
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
        return registrarRecepcion(
                nombreCompleto,
                documentoIdentidad,
                telefono,
                habitacionId,
                fechaIngreso,
                fechaSalida,
                List.of()
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
            List<DatosHuespedRecepcion> huespedesAdicionales
    ) {
        return ejecutorTransaccional.ejecutar(() -> {
            List<DatosHuespedRecepcion> adicionales
                    = huespedesAdicionales == null
                            ? List.of()
                            : huespedesAdicionales;

            Habitacion habitacion
                    = exigirHabitacionDisponibleParaActualizar(habitacionId);
            exigirDisponibilidadEnFechas(
                    habitacionId,
                    fechaIngreso,
                    fechaSalida
            );

            validarOcupacion(
                    habitacion,
                    documentoIdentidad,
                    adicionales
            );

            Cliente cliente = guardarOActualizarCliente(
                    nombreCompleto,
                    documentoIdentidad,
                    telefono
            );
            exigirHuespedDisponible(cliente.getId());

            Reserva reserva = reservaDAO.crear(new Reserva(
                    null,
                    proveedorHotelId.getHotelId(),
                    habitacion.getId(),
                    cliente.getId(),
                    fechaIngreso,
                    fechaSalida,
                    calcularTotalHospedaje(
                            habitacion.getPrecioPorNoche(),
                            fechaIngreso,
                            fechaSalida
                    ),
                    BigDecimal.ZERO,
                    EstadoReserva.ACTIVA
            ));
            reservaDAO.asociarHuesped(reserva.getId(), cliente.getId(), true);
            asociarHuespedesAdicionales(reserva, adicionales);

            habitacion.ocupar();
            persistir(habitacion);

            return reserva;
        });
    }

    private void asociarHuespedesAdicionales(
            Reserva reserva,
            List<DatosHuespedRecepcion> huespedesAdicionales
    ) {
        for (DatosHuespedRecepcion item : huespedesAdicionales) {
            validarHuespedAdicional(item);
            Cliente huesped = guardarOActualizarCliente(
                    item.nombreCompleto(),
                    item.documentoIdentidad(),
                    item.telefono()
            );
            exigirHuespedDisponible(huesped.getId());
            reservaDAO.asociarHuesped(
                    reserva.getId(),
                    huesped.getId(),
                    false
            );
        }
    }

    private void validarHuespedAdicional(DatosHuespedRecepcion item) {
        if (item == null
                || item.documentoIdentidad() == null
                || item.documentoIdentidad().isBlank()
                || item.nombreCompleto() == null
                || item.nombreCompleto().isBlank()) {
            throw new ReglaNegocioException(
                    "Cada huésped adicional debe tener DNI y nombre"
            );
        }
    }

    @Override
    public BigDecimal calcularTotalHospedaje(
            BigDecimal precioPorNoche,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida
    ) {
        Objects.requireNonNull(precioPorNoche, "precioPorNoche es obligatorio");
        if (precioPorNoche.signum() < 0) {
            throw new ReglaNegocioException(
                    "El precio por noche no puede ser negativo"
            );
        }
        if (fechaIngreso == null || fechaSalida == null
                || !fechaSalida.isAfter(fechaIngreso)) {
            throw new ReglaNegocioException(
                    "La salida debe ser posterior al ingreso"
            );
        }
        long dias = ChronoUnit.DAYS.between(
                fechaIngreso.toLocalDate(),
                fechaSalida.toLocalDate()
        );

        long diasFacturables = Math.max(1, dias);

        return precioPorNoche.multiply(
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

    /**
     * Registra la salida física del huésped (Check-out). Solo procede sobre
     * reservas ya finalizadas y transiciona el estado de la habitación a
     * EN_LIMPIEZA.
     *
     * METODO CLAVE DE NEGOCIO: - Cambia el estado de la reserva. - Aplica el
     * patrón State para cambiar el estado de la habitación mediante una
     * transición controlada.
     */
    @Override
    public Reserva registrarCheckOut(int reservaId) {
        return ejecutorTransaccional.ejecutar(() -> {
            Reserva reserva = buscarPorId(reservaId);
            if (reserva.getEstado() == EstadoReserva.CANCELADA) {
                throw new ReglaNegocioException(
                        "No se puede registrar la salida de una reserva cancelada"
                );
            }
            if (reserva.getEstado() == EstadoReserva.ACTIVA) {
                reserva.cambiarEstado(EstadoReserva.FINALIZADA);
            }
            Habitacion habitacion = buscarHabitacionDeReserva(reserva);

            if (habitacion.getEstado() == EstadoHabitacion.OCUPADA) {
                habitacion.iniciarLimpieza();
                persistir(habitacion);
            } else if (habitacion.getEstado()
                    != EstadoHabitacion.EN_LIMPIEZA) {
                throw new ReglaNegocioException(
                        "La habitación debe estar ocupada o en limpieza "
                        + "para registrar el check-out"
                );
            }
            persistir(reserva);

            return reserva;
        });
    }

    @Override
    public Reserva cancelar(int reservaId) {
        autorizadorAcceso.exigirAdministrador();
        Reserva reserva = exigirActiva(reservaId);
        reserva.cambiarEstado(EstadoReserva.CANCELADA);
        persistir(reserva);

        return reserva;
    }

    @Override
    public Reserva cancelarRecepcion(int reservaId) {
        autorizadorAcceso.exigirAdministrador();
        return ejecutorTransaccional.ejecutar(() -> {
            Reserva reserva = exigirActiva(reservaId);
            Habitacion habitacion = buscarHabitacionDeReserva(reserva);

            reserva.cambiarEstado(EstadoReserva.CANCELADA);
            if (habitacion.getEstado() == EstadoHabitacion.OCUPADA) {
                habitacion.iniciarLimpieza();
                persistir(habitacion);
            }
            persistir(reserva);
            return reserva;
        });
    }

    @Override
    public Reserva finalizar(int reservaId) {
        return ejecutorTransaccional.ejecutar(() -> {
            Reserva reserva = exigirActiva(reservaId);
            Habitacion habitacion = buscarHabitacionDeReserva(reserva);
            reserva.cambiarEstado(EstadoReserva.FINALIZADA);
            persistir(reserva);
            if (habitacion.getEstado() == EstadoHabitacion.OCUPADA) {
                habitacion.iniciarLimpieza();
                persistir(habitacion);
            }
            return reserva;
        });
    }

    @Override
    public boolean eliminar(int id) {
        autorizadorAcceso.exigirAdministrador();
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
                    "El cliente dejó de existir "
                    + "durante la actualización"
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
        return habitacionDAO.buscarPorId(
                reserva.getHabitacionId()).orElseThrow(
                () -> new EntidadNoEncontradaException(
                        "No existe la habitación de la reserva "
                        + reserva.getId()
                )
        );
    }

    private Habitacion exigirHabitacionDisponibleParaActualizar(
            int habitacionId
    ) {
        Habitacion habitacion = habitacionDAO
                .buscarPorIdParaActualizar(habitacionId)
                .orElseThrow(() -> new ReglaNegocioException(
                "La habitación no pertenece al hotel activo"
        ));
        if (habitacion.getEstado() != EstadoHabitacion.DISPONIBLE) {
            throw new ReglaNegocioException(
                    "Solo se pueden registrar reservas en habitaciones "
                    + "con estado DISPONIBLE"
            );
        }
        return habitacion;
    }

    private Cliente exigirHuespedDisponible(int clienteId) {
        Cliente cliente = clienteDAO.buscarPorIdParaActualizar(clienteId)
                .orElseThrow(() -> new ReglaNegocioException(
                "El huésped no pertenece al hotel activo"
        ));
        if (reservaDAO.existeReservaActivaParaCliente(clienteId)) {
            throw new ReglaNegocioException(
                    "El huésped " + cliente.getNombreCompleto()
                    + " ya pertenece a una reserva activa"
            );
        }
        return cliente;
    }

    private void exigirDisponibilidadEnFechas(
            int habitacionId,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida
    ) {
        if (fechaIngreso == null || fechaSalida == null
                || !fechaSalida.isAfter(fechaIngreso)) {
            throw new ReglaNegocioException(
                    "La salida debe ser posterior al ingreso"
            );
        }
        boolean existeCruce = reservaDAO.listar().stream()
                .filter(reserva -> reserva.getHabitacionId() == habitacionId)
                .filter(reserva -> reserva.getEstado() != EstadoReserva.CANCELADA)
                .anyMatch(reserva -> fechaIngreso.isBefore(
                reserva.getFechaSalida()
        ) && fechaSalida.isAfter(reserva.getFechaIngreso()));
        if (existeCruce) {
            throw new ReglaNegocioException(
                    "La habitación ya tiene una reserva en ese periodo"
            );
        }
    }

    private void validarOcupacion(
            Habitacion habitacion,
            String documentoPrincipal,
            List<DatosHuespedRecepcion> adicionales
    ) {
        int cantidadHuespedes = 1 + adicionales.size();
        if (cantidadHuespedes > habitacion.getCantidadCamas()) {
            throw new ReglaNegocioException(
                    "La habitación admite como máximo "
                    + habitacion.getCantidadCamas()
                    + " huésped(es) según su cantidad de camas"
            );
        }

        Set<String> documentos = new HashSet<>();
        if (documentoPrincipal != null) {
            documentos.add(documentoPrincipal.trim());
        }
        for (DatosHuespedRecepcion adicional : adicionales) {
            validarHuespedAdicional(adicional);
            String documento = adicional.documentoIdentidad().trim();
            if (!documentos.add(documento)) {
                throw new ReglaNegocioException(
                        "No se puede registrar el mismo DNI más de una vez"
                );
            }
        }
    }

    private void persistir(Reserva reserva) {
        if (!reservaDAO.actualizar(reserva)) {
            throw new EntidadNoEncontradaException(
                    "La reserva dejó de existir "
                    + "durante la actualización"
            );
        }
    }

    private void persistir(Habitacion habitacion) {
        if (!habitacionDAO.actualizar(habitacion)) {
            throw new EntidadNoEncontradaException(
                    "La habitación dejó de existir "
                    + "durante la actualización"
            );
        }
    }
}
