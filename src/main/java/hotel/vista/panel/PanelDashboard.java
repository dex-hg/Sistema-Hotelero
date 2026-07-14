package hotel.vista.panel;

import hotel.controlador.EstadisticasControlador;
import hotel.modelo.entidades.Usuario;
import hotel.modelo.servicio.ResumenDashboard;
import hotel.vista.VistaUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public final class PanelDashboard extends JPanel implements PanelActualizable {

    private final Usuario usuario;
    private final EstadisticasControlador estadisticas;

    private final JPanel tarjetas = new JPanel(new GridLayout(1, 4, 24, 0));
    private final JPanel avisos = new JPanel(new GridLayout(0, 1, 0, 10));

    public PanelDashboard(
            Usuario usuario,
            EstadisticasControlador estadisticas
    ) {
        super(new BorderLayout(18, 18));
        this.usuario = Objects.requireNonNull(usuario);
        this.estadisticas = Objects.requireNonNull(estadisticas);
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
                estadisticas::obtenerDashboard,
                this::aplicarDatos,
                null
        );
    }

    private void aplicarDatos(ResumenDashboard datos) {
        tarjetas.removeAll();
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Habitaciones disponibles",
                String.valueOf(datos.habitacionesDisponibles()),
                "DIS",
                new Color(25, 118, 210)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Habitaciones ocupadas",
                String.valueOf(datos.habitacionesOcupadas()),
                "OCU",
                new Color(20, 135, 84)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Reservas activas",
                String.valueOf(datos.reservasActivas()),
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
                + datos.habitacionesEnLimpieza()
        ));
        avisos.add(aviso(
                "Reservas activas por atender: "
                + datos.reservasActivas()
        ));
        avisos.add(aviso(
                "Habitaciones ocupadas actualmente: "
                + datos.habitacionesOcupadas()
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

}
