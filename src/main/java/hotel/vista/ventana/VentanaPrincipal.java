package hotel.vista.ventana;

import hotel.vista.panel.PanelHabitaciones;
import hotel.vista.panel.PanelRecepcion;
import hotel.vista.panel.PanelClientes;
import hotel.vista.panel.PanelReportes;
import hotel.vista.panel.PanelDashboard;
import hotel.vista.panel.PanelActualizable;
import hotel.configuracion.ComposicionAplicacion;

import hotel.controlador.AutenticacionControlador;

import hotel.modelo.entidades.Usuario;
import hotel.modelo.entidades.constantes.RolUsuario;
import hotel.vista.VistaUtil;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import java.util.Objects;

public final class VentanaPrincipal extends JFrame {

    private final ComposicionAplicacion aplicacion;
    private final AutenticacionControlador autenticacion;
    private final Usuario usuario;

    public VentanaPrincipal(
            ComposicionAplicacion aplicacion,
            Usuario usuario
    ) {
        super("HostelFlow");
        this.aplicacion = Objects.requireNonNull(aplicacion);
        this.autenticacion = aplicacion.autenticacionControlador();
        this.usuario = Objects.requireNonNull(usuario);
        construir();
    }

    public void mostrar() {
        if (SwingUtilities.isEventDispatchThread()) {
            setVisible(true);
        } else {
            SwingUtilities.invokeLater(() -> setVisible(true));
        }
    }

    private void construir() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        JLabel titulo = new JLabel("HostelFlow SaaS");
        titulo.setFont(
                titulo.getFont().deriveFont(
                        Font.BOLD, 22f
                )
        );

        JLabel sesion = new JLabel(
                "Usuario: " + usuario.getUsername()
                + " | Rol: " + VistaUtil.textoEnum(usuario.getRol())
        );

        JButton cerrarSesion = VistaUtil.botonPeligroCompacto("Cerrar sesión");
        cerrarSesion.addActionListener(e -> cerrarSesion());

        JPanel encabezado = new JPanel(new BorderLayout(12, 12));
        encabezado.setBorder(
                javax.swing.BorderFactory.createEmptyBorder(
                        14,
                        18,
                        14,
                        18
                )
        );
        JPanel identidad = new JPanel(new BorderLayout(10, 4));
        identidad.setOpaque(false);
        identidad.add(titulo, BorderLayout.NORTH);
        identidad.add(sesion, BorderLayout.SOUTH);

        encabezado.add(identidad, BorderLayout.WEST);
        encabezado.add(
                VistaUtil.envolverBotonCompacto(cerrarSesion),
                BorderLayout.EAST
        );

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Inicio", new PanelDashboard(
                usuario,
                aplicacion.habitacionControlador(),
                aplicacion.clienteControlador(),
                aplicacion.reservaControlador()
        ));

        if (usuario.getRol() == RolUsuario.RECEPCIONISTA) {
            tabs.addTab("Habitaciones", new PanelHabitaciones(
                    aplicacion.habitacionControlador(),
                    false
            ));
            tabs.addTab("Huéspedes", new PanelClientes(
                    aplicacion.clienteControlador(),
                    false
            ));
            tabs.addTab("Recepción", new PanelRecepcion(
                    aplicacion.clienteControlador(),
                    aplicacion.habitacionControlador(),
                    aplicacion.reservaControlador(),
                    false
            ));
        } else {
            tabs.addTab("Habitaciones", new PanelHabitaciones(
                    aplicacion.habitacionControlador(),
                    true
            ));

            tabs.addTab("Huéspedes", new PanelClientes(
                    aplicacion.clienteControlador(),
                    true
            )
            );

            tabs.addTab("Recepción", new PanelRecepcion(
                    aplicacion.clienteControlador(),
                    aplicacion.habitacionControlador(),
                    aplicacion.reservaControlador(),
                    true
            ));

            tabs.addTab("Reportes", new PanelReportes(
                    aplicacion.habitacionControlador(),
                    aplicacion.clienteControlador(),
                    aplicacion.reservaControlador()
            )
            );
        }

        tabs.addChangeListener(e -> {
            refrescarSeleccionado(tabs);
        });

        add(encabezado, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private void cerrarSesion() {
        if (!VistaUtil.confirmar(
                this,
                "¿Desea cerrar la sesión actual?",
                "Cerrar sesión"
        )) {
            return;
        }
        autenticacion.cerrarSesion();
        dispose();
        new VentanaLogin(aplicacion).mostrar();
    }

    private void refrescarSeleccionado(JTabbedPane tabs) {
        java.awt.Component seleccionado = tabs.getSelectedComponent();
        if (seleccionado instanceof PanelActualizable actualizable) {
            actualizable.refrescarAsync();
        }
    }
}
