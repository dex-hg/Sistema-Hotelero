package hotel.vista;

import hotel.configuracion.ComposicionAplicacion;
import hotel.controlador.AutenticacionControlador;
import hotel.modelo.entidades.Usuario;
import hotel.modelo.entidades.constantes.RolUsuario;

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

    public VentanaPrincipal(ComposicionAplicacion aplicacion, Usuario usuario) {
        super("HostelFlow SaaS");
        this.aplicacion = Objects.requireNonNull(aplicacion);
        this.autenticacion = aplicacion.autenticacionControlador();
        this.usuario = Objects.requireNonNull(usuario);
        construir();
    }

    public void mostrar() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    private void construir() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 680);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);

        JLabel titulo = new JLabel("HostelFlow SaaS");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));

        JLabel sesion = new JLabel("Usuario: " + usuario.getUsername()
                + " | Hotel: " + usuario.getHotelId()
                + " | Rol: " + usuario.getRol());

        JButton cerrarSesion = new JButton("Cerrar sesion");
        cerrarSesion.addActionListener(e -> cerrarSesion());

        JPanel encabezado = new JPanel(new BorderLayout(12, 12));
        encabezado.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 18, 14, 18));
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(sesion, BorderLayout.CENTER);
        encabezado.add(cerrarSesion, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        if (usuario.getRol() == RolUsuario.RECEPCIONISTA) {
            tabs.addTab("Habitaciones", new PanelHabitaciones(
                    aplicacion.habitacionControlador(),
                    false
            ));
            tabs.addTab("Recepcion", new PanelRecepcion(
                    aplicacion.clienteControlador(),
                    aplicacion.habitacionControlador(),
                    aplicacion.reservaControlador()
            ));
        } else {
            tabs.addTab("Habitaciones", new PanelHabitaciones(
                    aplicacion.habitacionControlador(),
                    true
            ));
            tabs.addTab("Clientes", new PanelClientes(aplicacion.clienteControlador()));
            tabs.addTab("Reservas", new PanelReservas(aplicacion.reservaControlador()));
        }

        add(encabezado, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private void cerrarSesion() {
        autenticacion.cerrarSesion();
        dispose();
        new VentanaLogin(aplicacion).mostrar();
    }
}
