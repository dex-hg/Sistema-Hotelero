package hotel.vista;

import hotel.configuracion.ComposicionAplicacion;
import hotel.controlador.AutenticacionControlador;
import hotel.modelo.entidades.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.Objects;

public final class VentanaLogin extends JFrame {

    private final ComposicionAplicacion aplicacion;
    private final AutenticacionControlador autenticacion;

    private final JTextField rucHotel = VistaUtil.campo("");
    private final JTextField username = VistaUtil.campo("");
    private final JPasswordField password = new JPasswordField(18);

    public VentanaLogin(ComposicionAplicacion aplicacion) {
        super("HostelFlow SaaS");
        this.aplicacion = Objects.requireNonNull(aplicacion);
        this.autenticacion = aplicacion.autenticacionControlador();
        construir();
    }

    public void mostrar() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    private void construir() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(580, 460);
        setMinimumSize(new Dimension(570, 450));
        setLocationRelativeTo(null);

        JLabel titulo = new JLabel("HostelFlow SaaS");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 28f));

        JLabel subtitulo = new JLabel("Inicia sesion con las credenciales de tu hotel");

        JPanel encabezado = new JPanel(new BorderLayout(4, 4));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(subtitulo, BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 8, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        agregarCampo(formulario, c, 0, "RUC del hotel", rucHotel);
        agregarCampo(formulario, c, 2, "Usuario", username);
        agregarCampo(formulario, c, 4, "Contraseña", password);

        JButton ingresar = new JButton("Iniciar sesion");
        ingresar.addActionListener(e -> VistaUtil.ejecutar(this, this::iniciarSesion));
        getRootPane().setDefaultButton(ingresar);

        JPanel acciones = new JPanel(new BorderLayout());
        acciones.add(ingresar, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout(18, 18));
        contenido.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(formulario, BorderLayout.CENTER);
        contenido.add(acciones, BorderLayout.SOUTH);

        setContentPane(contenido);
    }

    private void agregarCampo(
            JPanel formulario,
            GridBagConstraints c,
            int fila,
            String etiqueta,
            java.awt.Component campo
    ) {
        c.gridx = 0;
        c.gridy = fila;
        c.weighty = 0;
        formulario.add(new JLabel(etiqueta), c);

        c.gridy = fila + 1;
        c.weighty = 1;
        campo.setPreferredSize(new Dimension(260, 36));
        formulario.add(campo, c);
    }

    private void iniciarSesion() {
        Usuario usuario = autenticacion.iniciarSesion(
                rucHotel.getText(),
                username.getText(),
                new String(password.getPassword())
        );

        dispose();
        new VentanaPrincipal(aplicacion, usuario).mostrar();
    }
}
