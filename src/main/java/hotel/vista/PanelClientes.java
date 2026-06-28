package hotel.vista;

import hotel.controlador.ClienteControlador;
import hotel.modelo.entidades.Cliente;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;

import java.util.Objects;

public final class PanelClientes extends JPanel {

    private final ClienteControlador clientes;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Documento", "Telefono"},
            0
    );

    public PanelClientes(ClienteControlador clientes) {
        super(new BorderLayout(10, 10));
        this.clientes = Objects.requireNonNull(clientes);
        construir();
        refrescar();
    }

    private void construir() {
        JTable tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);

        JButton refrescar = new JButton("Refrescar");
        refrescar.addActionListener(e -> VistaUtil.ejecutar(this, this::refrescar));

        JButton crear = new JButton("Crear");
        crear.addActionListener(e -> VistaUtil.ejecutar(this, this::crear));

        JPanel acciones = new JPanel();
        acciones.add(refrescar);
        acciones.add(crear);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(acciones, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    private void refrescar() {
        modelo.setRowCount(0);
        for (Cliente cliente : clientes.listar()) {
            modelo.addRow(new Object[]{
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getTelefono() == null ? "-" : cliente.getTelefono()
            });
        }
    }

    private void crear() {
        JTextField nombre = VistaUtil.campo("");
        JTextField documento = VistaUtil.campo("");
        JTextField telefono = VistaUtil.campo("");

        if (!VistaUtil.confirmarFormulario(
                this,
                "Crear cliente",
                "Nombre completo", nombre,
                "Documento", documento,
                "Telefono", telefono
        )) {
            return;
        }

        clientes.crear(
                nombre.getText(),
                documento.getText(),
                telefono.getText().isBlank() ? null : telefono.getText()
        );
        refrescar();
    }
}
