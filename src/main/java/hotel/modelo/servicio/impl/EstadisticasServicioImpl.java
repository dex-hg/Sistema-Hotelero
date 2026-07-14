package hotel.modelo.servicio.impl;

import hotel.dao.ClienteDAO;
import hotel.dao.HabitacionDAO;
import hotel.dao.ReservaDAO;
import hotel.modelo.entidades.Cliente;
import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.modelo.servicio.EstadisticasServicio;
import hotel.modelo.servicio.ResumenDashboard;
import hotel.modelo.servicio.ResumenReportes;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Calcula indicadores del hotel fuera de Swing y trabaja solo contra DAOs.
 */
public final class EstadisticasServicioImpl implements EstadisticasServicio {

    private final HabitacionDAO habitacionDAO;
    private final ClienteDAO clienteDAO;
    private final ReservaDAO reservaDAO;

    public EstadisticasServicioImpl(
            HabitacionDAO habitacionDAO,
            ClienteDAO clienteDAO,
            ReservaDAO reservaDAO
    ) {
        this.habitacionDAO = Objects.requireNonNull(habitacionDAO);
        this.clienteDAO = Objects.requireNonNull(clienteDAO);
        this.reservaDAO = Objects.requireNonNull(reservaDAO);
    }

    @Override
    public ResumenDashboard obtenerDashboard() {
        Map<EstadoHabitacion, Integer> habitacionesPorEstado
                = contarHabitaciones(habitacionDAO.listar());
        Map<EstadoReserva, Integer> reservasPorEstado
                = contarReservas(reservaDAO.listar());
        return new ResumenDashboard(
                habitacionesPorEstado.getOrDefault(
                        EstadoHabitacion.DISPONIBLE,
                        0
                ),
                habitacionesPorEstado.getOrDefault(
                        EstadoHabitacion.OCUPADA,
                        0
                ),
                habitacionesPorEstado.getOrDefault(
                        EstadoHabitacion.EN_LIMPIEZA,
                        0
                ),
                reservasPorEstado.getOrDefault(EstadoReserva.ACTIVA, 0),
                clienteDAO.listar().size()
        );
    }

    @Override
    public ResumenReportes obtenerReportes(YearMonth periodo) {
        Objects.requireNonNull(periodo, "periodo es obligatorio");
        List<Habitacion> habitaciones = habitacionDAO.listar();
        List<Cliente> clientes = clienteDAO.listar();
        List<Reserva> reservas = reservaDAO.listar();
        return new ResumenReportes(
                habitacionMasReservada(reservas, habitaciones),
                diasPromedio(reservas),
                valorReservasMes(reservas, periodo),
                clientes.size(),
                huespedesRecurrentes(
                        reservas,
                        reservaDAO.listarHuespedesPorReserva()
                ),
                contarReservas(reservas)
        );
    }

    private Map<EstadoHabitacion, Integer> contarHabitaciones(
            List<Habitacion> habitaciones
    ) {
        Map<EstadoHabitacion, Integer> conteo = new EnumMap<>(
                EstadoHabitacion.class
        );
        for (Habitacion habitacion : habitaciones) {
            conteo.merge(habitacion.getEstado(), 1, Integer::sum);
        }
        return conteo;
    }

    private Map<EstadoReserva, Integer> contarReservas(
            List<Reserva> reservas
    ) {
        Map<EstadoReserva, Integer> conteo = new EnumMap<>(
                EstadoReserva.class
        );
        for (Reserva reserva : reservas) {
            conteo.merge(reserva.getEstado(), 1, Integer::sum);
        }
        return conteo;
    }

    private String habitacionMasReservada(
            List<Reserva> reservas,
            List<Habitacion> habitaciones
    ) {
        Map<Integer, Integer> conteo = new HashMap<>();
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() != EstadoReserva.CANCELADA) {
                conteo.merge(reserva.getHabitacionId(), 1, Integer::sum);
            }
        }
        Integer habitacionId = conteo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        if (habitacionId == null) {
            return "-";
        }
        return habitaciones.stream()
                .filter(habitacion -> habitacion.getId().equals(habitacionId))
                .map(Habitacion::getNumero)
                .findFirst()
                .orElse(String.valueOf(habitacionId));
    }

    private double diasPromedio(List<Reserva> reservas) {
        return reservas.stream()
                .filter(reserva -> reserva.getEstado()
                == EstadoReserva.FINALIZADA)
                .mapToLong(reserva -> Math.max(1, ChronoUnit.DAYS.between(
                reserva.getFechaIngreso().toLocalDate(),
                reserva.getFechaSalida().toLocalDate()
        )))
                .average()
                .orElse(0);
    }

    private BigDecimal valorReservasMes(
            List<Reserva> reservas,
            YearMonth periodo
    ) {
        return reservas.stream()
                .filter(reserva -> reserva.getEstado()
                != EstadoReserva.CANCELADA)
                .filter(reserva -> YearMonth.from(reserva.getFechaIngreso())
                .equals(periodo))
                .map(Reserva::getTotalHospedaje)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int huespedesRecurrentes(
            List<Reserva> reservas,
            Map<Integer, List<Cliente>> huespedesPorReserva
    ) {
        Map<Integer, Integer> conteo = new HashMap<>();
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() == EstadoReserva.CANCELADA) {
                continue;
            }
            List<Cliente> huespedes = huespedesPorReserva.getOrDefault(
                    reserva.getId(),
                    List.of()
            );
            if (huespedes.isEmpty()) {
                conteo.merge(reserva.getClienteId(), 1, Integer::sum);
                continue;
            }
            for (Cliente huesped : huespedes) {
                conteo.merge(huesped.getId(), 1, Integer::sum);
            }
        }
        return (int) conteo.values().stream()
                .filter(cantidad -> cantidad > 1)
                .count();
    }
}
