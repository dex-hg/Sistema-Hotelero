package hotel.vista.panel;

import hotel.controlador.ClienteControlador;
import hotel.modelo.entidades.Cliente;
import hotel.vista.VistaUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.Objects;

public final class PanelClientes extends JPanel implements PanelActualizable {

    private final ClienteControlador clientes;
    private final boolean permitirEliminar;
    private final JTextField busqueda = VistaUtil.campo("");
    private final JTextField id = VistaUtil.campoLectura("");
    private final JTextField nombre = VistaUtil.campo("");
    private final JTextField documento = VistaUtil.campo("");
    private final JTextField telefono = VistaUtil.campo("");
    private final DefaultTableModel modelo = VistaUtil.modeloTabla(
            new Object[]{"ID", "Nombre", "DNI", "Teléfono"},
            new Class<?>[]{Integer.class, String.class, String.class, String.class}
    );
    private JTable tabla;
    private JButton botonActualizar;
    private JButton botonEliminar;

    public PanelClientes(
            ClienteControlador clientes,
            boolean permitirEliminar
    ) {
        super(new BorderLayout(18, 18));
        this.clientes = Objects.requireNonNull(clientes);
        this.permitirEliminar = permitirEliminar;
        construir();
        refrescarAsync();
    }

    private void construir() {
        JButton refrescar = VistaUtil.botonCompacto("Refrescar");
        refrescar.addActionListener(e -> refrescarAsync());

        setBorder(
                javax.swing.BorderFactory.createEmptyBorder(
                        18,
                        18,
                        18,
                        18
                )
        );
        add(VistaUtil.encabezadoModulo(
                "Catálogo y gestión de huéspedes",
                permitirEliminar
                        ? "Registre, modifique o elimine huéspedes"
                        : "Registre huéspedes y modifique sus datos",
                refrescar
        ), BorderLayout.NORTH);

        tabla = new JTable(modelo);
        TableRowSorter<DefaultTableModel> ordenador
                = VistaUtil.configurarTabla(tabla, modelo);
        VistaUtil.conectarBusquedaPorColumna(
                busqueda,
                ordenador,
                2
        );
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarCliente();
            }
        });

        JPanel contenido = new JPanel(new BorderLayout(18, 18));
        contenido.add(construirFormulario(), BorderLayout.WEST);
        contenido.add(construirListado(), BorderLayout.CENTER);
        add(contenido, BorderLayout.CENTER);
    }

    private JPanel construirFormulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        agregarCampo(formulario, c, 0, "Nombre completo *", nombre);
        agregarCampo(formulario, c, 1, "DNI *", documento);
        agregarCampo(formulario, c, 2, "Teléfono", telefono);

        JPanel acciones = new JPanel(new GridBagLayout());
        GridBagConstraints a = new GridBagConstraints();
        a.insets = new Insets(5, 0, 5, 0);
        a.fill = GridBagConstraints.HORIZONTAL;
        a.weightx = 1;
        a.gridx = 0;

        JButton crear = VistaUtil.botonPrimario("Registrar huésped");
        crear.addActionListener(e -> VistaUtil.ejecutar(
                this,
                () -> crearAsync(crear)
        ));
        agregarBoton(acciones, a, crear);

        botonActualizar = VistaUtil.botonSecundario("Actualizar huésped");
        botonActualizar.setEnabled(false);
        botonActualizar.addActionListener(e -> VistaUtil.ejecutar(
                this,
                this::actualizarAsync
        ));
        agregarBoton(acciones, a, botonActualizar);

        if (permitirEliminar) {
            botonEliminar = VistaUtil.botonPeligro("Eliminar huésped");
            botonEliminar.setEnabled(false);
            botonEliminar.addActionListener(e -> VistaUtil.ejecutar(
                    this,
                    this::eliminarAsync
            ));
            agregarBoton(acciones, a, botonEliminar);
        }

        JButton limpiar = VistaUtil.botonSecundario("Limpiar formulario");
        limpiar.addActionListener(e -> limpiarFormulario());
        agregarBoton(acciones, a, limpiar);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(formulario, BorderLayout.NORTH);
        panel.add(acciones, BorderLayout.CENTER);
        return VistaUtil.seccion("Detalles del huésped", panel);
    }

    private JPanel construirListado() {
        JPanel filtros = new JPanel();
        JLabel busquedaLabel = new JLabel("Buscar por DNI");
        busquedaLabel.setLabelFor(busqueda);
        filtros.add(busquedaLabel);
        filtros.add(busqueda);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(filtros, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return VistaUtil.seccion("Huéspedes registrados", panel);
    }

    private void agregarCampo(
            JPanel formulario,
            GridBagConstraints c,
            int fila,
            String etiqueta,
            java.awt.Component campo
    ) {
        c.gridy = fila * 2;
        c.gridx = 0;
        JLabel label = new JLabel(etiqueta);
        label.setLabelFor(campo);
        formulario.add(label, c);

        c.gridy = fila * 2 + 1;
        formulario.add(campo, c);
    }

    private void agregarBoton(
            JPanel panel,
            GridBagConstraints c,
            JButton boton
    ) {
        c.gridy++;
        panel.add(boton, c);
    }

    @Override
    public void refrescarAsync() {
        VistaUtil.ejecutarAsync(
                this,
                null,
                clientes::listar,
                this::cargarClientes,
                null
        );
    }

    private void cargarClientes(java.util.List<Cliente> listado) {
        modelo.setRowCount(0);
        for (Cliente cliente : listado) {
            modelo.addRow(new Object[]{
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getTelefono() == null ? "" : cliente.getTelefono()
            });
        }
        limpiarFormulario();
    }

    private void seleccionarCliente() {
        Integer seleccionado = VistaUtil.idFilaSeleccionada(tabla);
        if (seleccionado == null) {
            return;
        }

        int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
        id.setText(String.valueOf(modelo.getValueAt(fila, 0)));
        nombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        documento.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        telefono.setText(String.valueOf(modelo.getValueAt(fila, 3)));
        botonActualizar.setEnabled(true);
        if (botonEliminar != null) {
            botonEliminar.setEnabled(true);
        }
    }

    private void crearAsync(JButton boton) {
        String nombreValor = nombre.getText();
        String documentoValor = documento.getText();
        String telefonoValor = telefono.getText().isBlank()
                ? null : telefono.getText().trim();
        VistaUtil.ejecutarAsync(
                this,
                boton,
                () -> clientes.crear(nombreValor, documentoValor, telefonoValor),
                creado -> {
                    limpiarFormulario();
                    refrescarAsync();
                },
                "El huésped se registró correctamente"
        );
    }

    private void actualizarAsync() {
        int clienteId = VistaUtil.entero(id.getText());
        String nombreValor = nombre.getText();
        String documentoValor = documento.getText();
        String telefonoValor = telefono.getText().isBlank()
                ? null : telefono.getText().trim();
        VistaUtil.ejecutarAsync(
                this,
                botonActualizar,
                () -> {
                    Cliente actual = clientes.buscarPorId(clienteId);
                    boolean actualizado = clientes.actualizar(new Cliente(
                            actual.getId(),
                            actual.getHotelId(),
                            nombreValor,
                            documentoValor,
                            telefonoValor
                    ));
                    VistaUtil.exigirExito(
                            actualizado,
                            "El huésped ya no existe"
                    );
                    return actualizado;
                },
                actualizado -> {
                    limpiarFormulario();
                    refrescarAsync();
                },
                "El huésped se actualizó correctamente"
        );
    }

    private void eliminarAsync() {
        if (!VistaUtil.confirmar(
                this,
                "¿Desea eliminar el huésped seleccionado?",
                "Confirmar eliminación"
        )) {
            return;
        }
        int clienteId = VistaUtil.entero(id.getText());
        VistaUtil.ejecutarAsync(
                this,
                botonEliminar,
                () -> {
                    boolean eliminado = clientes.eliminar(clienteId);
                    VistaUtil.exigirExito(eliminado, "El huésped ya no existe");
                    return eliminado;
                },
                eliminado -> {
                    limpiarFormulario();
                    refrescarAsync();
                },
                "El huésped se eliminó correctamente"
        );
    }

    private void limpiarFormulario() {
        id.setText("");
        nombre.setText("");
        documento.setText("");
        telefono.setText("");
        tabla.clearSelection();
        if (botonActualizar != null) {
            botonActualizar.setEnabled(false);
        }
        if (botonEliminar != null) {
            botonEliminar.setEnabled(false);
        }
    }
}
