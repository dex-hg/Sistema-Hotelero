package hotel.vista.panel;

import hotel.controlador.ClienteControlador;
import hotel.controlador.HabitacionControlador;
import hotel.controlador.ReservaControlador;

import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.Usuario;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.vista.VistaUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class PanelDashboard extends JPanel implements PanelActualizable {

    private final Usuario usuario;
    private final HabitacionControlador habitaciones;
    private final ClienteControlador clientes;
    private final ReservaControlador reservas;

    private final JPanel tarjetas = new JPanel(new GridLayout(1, 4, 24, 0));
    private final JPanel avisos = new JPanel(new GridLayout(0, 1, 0, 10));

    public PanelDashboard(
            Usuario usuario,
            HabitacionControlador habitaciones,
            ClienteControlador clientes,
            ReservaControlador reservas
    ) {
        super(new BorderLayout(18, 18));
        this.usuario = Objects.requireNonNull(usuario);
        this.habitaciones = Objects.requireNonNull(habitaciones);
        this.clientes = Objects.requireNonNull(clientes);
        this.reservas = Objects.requireNonNull(reservas);
        construir();
        refrescarAsync();
    }

    private void construir() {
        JButton refrescar = VistaUtil.botonCompacto("Actualizar datos");
        refrescar.addActionListener(e -> refrescarAsync());

        setBorder(
                javax.swing.BorderFactory.createEmptyBorder(
                        24,
                        24,
                        24,
                        24
                )
        );
        add(VistaUtil.encabezadoModulo(
                "Bienvenido, " + usuario.getUsername(),
                "Hoy es " + fechaActual()
                + " | Rol: " + VistaUtil.textoEnum(usuario.getRol()),
                refrescar
        ), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(18, 18));
        contenido.setOpaque(false);
        contenido.add(tarjetas, BorderLayout.NORTH);
        contenido.add(
                VistaUtil.seccion(
                        "Avisos",
                        avisos),
                BorderLayout.SOUTH
        );
        add(contenido, BorderLayout.CENTER);
    }

    @Override
    public void refrescarAsync() {
        VistaUtil.ejecutarAsync(
                this,
                null,
                () -> {
                    java.util.List<Reserva> listadoReservas = reservas.listar();
                    return new DatosDashboard(
                            habitaciones.listar(),
                            clientes.listar().size(),
                            listadoReservas
                    );
                },
                this::aplicarDatos,
                null
        );
    }

    private void aplicarDatos(DatosDashboard datos) {
        Map<EstadoHabitacion, Integer> porHabitacion = contarHabitaciones(
                datos.habitaciones()
        );
        Map<EstadoReserva, Integer> porReserva = contarReservas(
                datos.reservas()
        );

        int disponibles = porHabitacion.getOrDefault(
                EstadoHabitacion.DISPONIBLE,
                0
        );
        int ocupadas = porHabitacion.getOrDefault(
                EstadoHabitacion.OCUPADA,
                0
        );
        int limpieza = porHabitacion.getOrDefault(
                EstadoHabitacion.EN_LIMPIEZA,
                0
        );
        int activas = porReserva.getOrDefault(
                EstadoReserva.ACTIVA,
                0
        );

        tarjetas.removeAll();
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Habitaciones disponibles",
                String.valueOf(disponibles),
                "DIS",
                new Color(25, 118, 210)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Habitaciones ocupadas",
                String.valueOf(ocupadas),
                "OCU",
                new Color(20, 135, 84)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Reservas activas",
                String.valueOf(activas),
                "RES",
                new Color(230, 126, 34)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Huéspedes registrados",
                String.valueOf(datos.cantidadClientes()),
                "HUE",
                new Color(111, 66, 193)
        ));

        avisos.removeAll();
        avisos.add(aviso(
                "Habitaciones pendientes de limpieza: "
                + limpieza
        ));
        avisos.add(aviso(
                "Reservas activas por atender: "
                + activas
        ));
        avisos.add(aviso(
                "Habitaciones ocupadas actualmente: "
                + ocupadas
        ));

        revalidate();
        repaint();
    }

    private JLabel aviso(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setBorder(
                javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createMatteBorder(
                                0,
                                4,
                                0,
                                0,
                                new Color(25, 118, 210)
                        ),
                        javax.swing.BorderFactory.createEmptyBorder(
                                12,
                                14,
                                12,
                                14
                        )
                )
        );
        etiqueta.setOpaque(true);
        etiqueta.setBackground(Color.WHITE);
        return etiqueta;
    }

    private String fechaActual() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern(
                "EEEE, d 'de' MMMM 'de' yyyy",
                Locale.forLanguageTag("es-PE")
        );
        return LocalDate.now().format(formato);
    }

    private Map<EstadoHabitacion, Integer> contarHabitaciones(
            java.util.List<Habitacion> listado
    ) {
        Map<EstadoHabitacion, Integer> conteo = new EnumMap<>(
                EstadoHabitacion.class
        );
        for (Habitacion habitacion : listado) {
            conteo.merge(habitacion.getEstado(), 1, Integer::sum);
        }
        return conteo;
    }

    private Map<EstadoReserva, Integer> contarReservas(
            java.util.List<Reserva> listado
    ) {
        Map<EstadoReserva, Integer> conteo = new EnumMap<>(
                EstadoReserva.class
        );
        for (Reserva reserva : listado) {
            conteo.merge(reserva.getEstado(), 1, Integer::sum);
        }
        return conteo;
    }

    private record DatosDashboard(
            java.util.List<Habitacion> habitaciones,
            int cantidadClientes,
            java.util.List<Reserva> reservas
    ) {
    }
}
