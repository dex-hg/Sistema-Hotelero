package hotel.vista;

import hotel.controlador.ClienteControlador;
import hotel.controlador.HabitacionControlador;
import hotel.controlador.ReservaControlador;
import hotel.modelo.entidades.Cliente;
import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.constantes.EstadoHabitacion;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class PanelRecepcion extends JPanel {

    private final ClienteControlador clientes;
    private final HabitacionControlador habitaciones;
    private final ReservaControlador reservas;

    private final JTextField documento = VistaUtil.campo("");
    private final JTextField nombre = VistaUtil.campo("");
    private final JTextField telefono = VistaUtil.campo("");
    private final JComboBox<ItemHabitacion> habitacion = new JComboBox<>();
    private final JTextField ingreso = VistaUtil.campo("2026-07-01 14:00");
    private final JTextField salida = VistaUtil.campo("2026-07-02 12:00");
    private final JTextField totalCalculado = VistaUtil.campo("0.00");
    private final JTextField reservaCheckOut = VistaUtil.campo("");

    private final DefaultTableModel modeloReservas = new DefaultTableModel(
            new Object[]{
                "ID", "Habitacion", "Cliente", "Ingreso", "Salida", "Pagado", "Estado"
            },
            0
    );

    public PanelRecepcion(
            ClienteControlador clientes,
            HabitacionControlador habitaciones,
            ReservaControlador reservas
    ) {
        super(new BorderLayout(12, 12));
        this.clientes = Objects.requireNonNull(clientes);
        this.habitaciones = Objects.requireNonNull(habitaciones);
        this.reservas = Objects.requireNonNull(reservas);
        construir();
        refrescar();
    }

    private void construir() {
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        agregarCampo(formulario, c, 0, 0, "DNI", documento);
        agregarCampo(formulario, c, 0, 2, "Nombre completo", nombre);
        agregarCampo(formulario, c, 1, 0, "Telefono", telefono);
        agregarCampo(formulario, c, 1, 2, "Habitacion disponible", habitacion);
        agregarCampo(formulario, c, 2, 0, "Ingreso", ingreso);
        agregarCampo(formulario, c, 2, 2, "Salida", salida);
        totalCalculado.setEditable(false);
        agregarCampo(formulario, c, 3, 0, "Total calculado", totalCalculado);
        configurarActualizacionTotal();

        JButton buscarDni = new JButton("Buscar DNI");
        buscarDni.addActionListener(e -> VistaUtil.ejecutar(this, this::buscarCliente));

        JButton registrar = new JButton("Registrar check-in");
        registrar.addActionListener(e -> VistaUtil.ejecutar(this, this::registrarCheckIn));

        JButton refrescar = new JButton("Refrescar");
        refrescar.addActionListener(e -> VistaUtil.ejecutar(this, this::refrescar));

        JPanel accionesRegistro = new JPanel();
        accionesRegistro.add(buscarDni);
        accionesRegistro.add(registrar);
        accionesRegistro.add(refrescar);

        JPanel registro = new JPanel(new BorderLayout(8, 8));
        registro.setBorder(javax.swing.BorderFactory.createTitledBorder("Registro de huesped"));
        registro.add(formulario, BorderLayout.CENTER);
        registro.add(accionesRegistro, BorderLayout.SOUTH);

        JTable tabla = new JTable(modeloReservas);
        tabla.setAutoCreateRowSorter(true);

        JButton checkOut = new JButton("Registrar check-out");
        checkOut.addActionListener(e -> VistaUtil.ejecutar(this, this::registrarCheckOut));

        JPanel salidaPanel = new JPanel();
        salidaPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Salida"));
        salidaPanel.add(new JLabel("Reserva ID"));
        salidaPanel.add(reservaCheckOut);
        salidaPanel.add(checkOut);

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        centro.add(salidaPanel, BorderLayout.SOUTH);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(registro, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    private void agregarCampo(
            JPanel formulario,
            GridBagConstraints c,
            int fila,
            int columna,
            String etiqueta,
            java.awt.Component campo
    ) {
        c.gridy = fila * 2;
        c.gridx = columna;
        formulario.add(new JLabel(etiqueta), c);

        c.gridy = fila * 2 + 1;
        c.gridx = columna;
        c.gridwidth = 2;
        formulario.add(campo, c);
        c.gridwidth = 1;
    }

    private void buscarCliente() {
        clientes.buscarPorDocumento(documento.getText())
                .ifPresentOrElse(cliente -> {
                    nombre.setText(cliente.getNombreCompleto());
                    telefono.setText(cliente.getTelefono() == null ? "" : cliente.getTelefono());
                }, () -> javax.swing.JOptionPane.showMessageDialog(
                this,
                "No hay cliente registrado con ese DNI. Complete sus datos.",
                "Cliente nuevo",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
        ));
    }

    private void registrarCheckIn() {
        ItemHabitacion item = (ItemHabitacion) habitacion.getSelectedItem();
        if (item == null) {
            throw new IllegalArgumentException("No hay habitaciones disponibles para registrar check-in");
        }

        reservas.registrarRecepcion(
                nombre.getText(),
                documento.getText(),
                telefono.getText().isBlank() ? null : telefono.getText(),
                item.id,
                VistaUtil.fecha(ingreso.getText()),
                VistaUtil.fecha(salida.getText())
        );

        limpiarFormulario();
        refrescar();
    }

    private void registrarCheckOut() {
        reservas.registrarCheckOut(VistaUtil.entero(reservaCheckOut.getText()));
        reservaCheckOut.setText("");
        refrescar();
    }

    private void refrescar() {
        cargarHabitacionesDisponibles();
        cargarReservas();
    }

    private void cargarHabitacionesDisponibles() {
        habitacion.removeAllItems();
        for (Habitacion item : habitaciones.listar()) {
            if (item.getEstado() == EstadoHabitacion.DISPONIBLE) {
                habitacion.addItem(new ItemHabitacion(
                        item.getId(),
                        item.getNumero(),
                        item.getPrecioPorNoche()
                ));
            }
        }
        actualizarTotalCalculado();
    }

    private void cargarReservas() {
        modeloReservas.setRowCount(0);
        for (Reserva reserva : reservas.listar()) {
            modeloReservas.addRow(new Object[]{
                reserva.getId(),
                reserva.getHabitacionId(),
                reserva.getClienteId(),
                VistaUtil.FORMATO_FECHA.format(reserva.getFechaIngreso()),
                VistaUtil.FORMATO_FECHA.format(reserva.getFechaSalida()),
                reserva.getTotalPagado(),
                reserva.getEstado()
            });
        }
    }

    private void limpiarFormulario() {
        documento.setText("");
        nombre.setText("");
        telefono.setText("");
        actualizarTotalCalculado();
    }

    private void configurarActualizacionTotal() {
        habitacion.addActionListener(e -> actualizarTotalCalculado());

        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarTotalCalculado();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarTotalCalculado();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarTotalCalculado();
            }
        };

        ingreso.getDocument().addDocumentListener(listener);
        salida.getDocument().addDocumentListener(listener);
    }

    private void actualizarTotalCalculado() {
        ItemHabitacion item = (ItemHabitacion) habitacion.getSelectedItem();
        if (item == null) {
            totalCalculado.setText("0.00");
            return;
        }

        try {
            LocalDateTime fechaIngreso = VistaUtil.fecha(ingreso.getText());
            LocalDateTime fechaSalida = VistaUtil.fecha(salida.getText());
            long dias = ChronoUnit.DAYS.between(
                    fechaIngreso.toLocalDate(),
                    fechaSalida.toLocalDate()
            );
            long diasFacturables = Math.max(1, dias);

            totalCalculado.setText(
                    item.precioPorNoche.multiply(BigDecimal.valueOf(diasFacturables))
                            .toPlainString()
            );
        } catch (RuntimeException e) {
            totalCalculado.setText("Fecha invalida");
        }
    }

    private static final class ItemHabitacion {

        private final int id;
        private final String numero;
        private final BigDecimal precioPorNoche;

        private ItemHabitacion(
                int id,
                String numero,
                BigDecimal precioPorNoche
        ) {
            this.id = id;
            this.numero = numero;
            this.precioPorNoche = precioPorNoche;
        }

        @Override
        public String toString() {
            return numero + " - " + precioPorNoche + " por noche";
        }
    }
}
