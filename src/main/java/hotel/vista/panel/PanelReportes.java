package hotel.vista.panel;

import hotel.controlador.ClienteControlador;
import hotel.controlador.HabitacionControlador;
import hotel.controlador.ReservaControlador;

import hotel.modelo.entidades.Cliente;
import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.vista.VistaUtil;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PanelReportes extends JPanel implements PanelActualizable {

    private final HabitacionControlador habitaciones;
    private final ClienteControlador clientes;
    private final ReservaControlador reservas;

    private final JPanel tarjetas = new JPanel(
            new GridLayout(2, 3, 18, 18)
    );
    private final DefaultTableModel modeloEstados = VistaUtil.modeloTabla(
            new Object[]{"Estado", "Cantidad"},
            new Class<?>[]{EstadoReserva.class, Integer.class}
    );

    public PanelReportes(
            HabitacionControlador habitaciones,
            ClienteControlador clientes,
            ReservaControlador reservas
    ) {
        super(new BorderLayout(18, 18));
        this.habitaciones = Objects.requireNonNull(habitaciones);
        this.clientes = Objects.requireNonNull(clientes);
        this.reservas = Objects.requireNonNull(reservas);
        construir();
        refrescarAsync();
    }

    private void construir() {
        JButton actualizar = VistaUtil.botonCompacto("Actualizar datos");
        actualizar.addActionListener(e -> refrescarAsync());

        setBorder(
                javax.swing.BorderFactory.createEmptyBorder(
                        18,
                        18,
                        18,
                        18
                )
        );
        add(VistaUtil.encabezadoModulo(
                "Reportes administrativos",
                "Indicadores útiles para el dueño del negocio",
                actualizar
        ), BorderLayout.NORTH);

        JTable tablaEstados = new JTable(modeloEstados);
        VistaUtil.configurarTabla(tablaEstados, modeloEstados);

        JPanel contenido = new JPanel(new BorderLayout(18, 18));
        contenido.add(tarjetas, BorderLayout.NORTH);
        contenido.add(
                VistaUtil.seccion(
                        "Reservas por estado",
                        new JScrollPane(tablaEstados)
                ),
                BorderLayout.CENTER
        );
        add(contenido, BorderLayout.CENTER);
    }

    @Override
    public void refrescarAsync() {
        VistaUtil.ejecutarAsync(
                this,
                null,
                () -> {
                    List<Reserva> listadoReservas = reservas.listar();
                    return new DatosReportes(
                            habitaciones.listar(),
                            clientes.listar(),
                            listadoReservas,
                            reservas.listarHuespedesPorReserva()
                    );
                },
                this::aplicarDatos,
                null
        );
    }

    private void aplicarDatos(DatosReportes datos) {
        List<Reserva> listadoReservas = datos.reservas();
        List<Cliente> listadoClientes = datos.clientes();
        Map<Integer, Habitacion> habitacionesPorId = habitacionesPorId(
                datos.habitaciones()
        );

        tarjetas.removeAll();
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Habitación más reservada",
                habitacionMasOcupada(listadoReservas, habitacionesPorId),
                "HAB",
                new Color(25, 118, 210)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Días promedio de estadía",
                diasPromedio(listadoReservas),
                "DÍAS",
                new Color(20, 135, 84)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Valor de reservas del mes",
                totalMes(listadoReservas),
                "S/",
                new Color(230, 126, 34)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Huéspedes registrados",
                String.valueOf(listadoClientes.size()),
                "HUE",
                new Color(111, 66, 193)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Huéspedes recurrentes",
                String.valueOf(huespedesRecurrentes(
                        listadoReservas,
                        datos.huespedesPorReserva()
                )),
                "REC",
                new Color(13, 110, 253)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Reservas activas",
                String.valueOf(contarEstado(
                        listadoReservas,
                        EstadoReserva.ACTIVA
                )),
                "ACT",
                new Color(190, 45, 55)
        ));

        cargarEstados(listadoReservas);
        revalidate();
        repaint();
    }

    private Map<Integer, Habitacion> habitacionesPorId(
            List<Habitacion> listado
    ) {
        Map<Integer, Habitacion> resultado = new HashMap<>();
        for (Habitacion habitacion : listado) {
            resultado.put(habitacion.getId(), habitacion);
        }
        return resultado;
    }

    private String habitacionMasOcupada(
            List<Reserva> listadoReservas,
            Map<Integer, Habitacion> habitacionesPorId
    ) {
        Map<Integer, Integer> conteo = new HashMap<>();
        for (Reserva reserva : listadoReservas) {
            if (reserva.getEstado() != EstadoReserva.CANCELADA) {
                conteo.merge(reserva.getHabitacionId(), 1, Integer::sum);
            }
        }

        int habitacionId = conteo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
        if (habitacionId == 0) {
            return "-";
        }

        Habitacion habitacion = habitacionesPorId.get(habitacionId);
        return habitacion == null
                ? String.valueOf(habitacionId)
                : habitacion.getNumero();
    }

    private String diasPromedio(List<Reserva> listadoReservas) {
        List<Reserva> finalizadas = listadoReservas.stream()
                .filter(reserva -> reserva.getEstado()
                == EstadoReserva.FINALIZADA)
                .toList();
        if (finalizadas.isEmpty()) {
            return "0";
        }

        double promedio = finalizadas.stream()
                .mapToLong(reserva -> Math.max(1, ChronoUnit.DAYS.between(
                reserva.getFechaIngreso().toLocalDate(),
                reserva.getFechaSalida().toLocalDate()
        )))
                .average()
                .orElse(0);
        return String.format("%.1f", promedio);
    }

    private String totalMes(List<Reserva> listadoReservas) {
        YearMonth actual = YearMonth.now();
        BigDecimal total = BigDecimal.ZERO;
        for (Reserva reserva : listadoReservas) {
            if (reserva.getEstado() != EstadoReserva.CANCELADA
                    && YearMonth.from(reserva.getFechaIngreso())
                            .equals(actual)) {
                total = total.add(reserva.getTotalPagado());
            }
        }
        return total.toPlainString();
    }

    private int huespedesRecurrentes(
            List<Reserva> listadoReservas,
            Map<Integer, List<Cliente>> huespedesPorReserva
    ) {
        Map<Integer, Integer> conteo = new HashMap<>();
        for (Reserva reserva : listadoReservas) {
            if (reserva.getEstado() == EstadoReserva.CANCELADA) {
                continue;
            }
            List<Cliente> huespedes = huespedesPorReserva.getOrDefault(
                    reserva.getId(),
                    List.of()
            );
            if (huespedes.isEmpty()) {
                conteo.merge(reserva.getClienteId(), 1, Integer::sum);
            } else {
                for (Cliente huesped : huespedes) {
                    conteo.merge(huesped.getId(), 1, Integer::sum);
                }
            }
        }
        return (int) conteo.values().stream()
                .filter(cantidad -> cantidad > 1)
                .count();
    }

    private int contarEstado(
            List<Reserva> listadoReservas,
            EstadoReserva estado
    ) {
        return (int) listadoReservas.stream()
                .filter(reserva -> reserva.getEstado() == estado)
                .count();
    }

    private void cargarEstados(List<Reserva> listadoReservas) {
        modeloEstados.setRowCount(0);
        for (EstadoReserva estado : EstadoReserva.values()) {
            modeloEstados.addRow(new Object[]{
                estado,
                contarEstado(listadoReservas, estado)
            });
        }
    }

    private record DatosReportes(
            List<Habitacion> habitaciones,
            List<Cliente> clientes,
            List<Reserva> reservas,
            Map<Integer, List<Cliente>> huespedesPorReserva
    ) {
    }
}
