package hotel.vista.panel;

import hotel.controlador.ClienteControlador;
import hotel.controlador.HabitacionControlador;
import hotel.controlador.ReservaControlador;

import hotel.modelo.entidades.Cliente;
import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.modelo.servicio.DatosHuespedRecepcion;
import hotel.vista.VistaUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;

public final class PanelRecepcion extends JPanel implements PanelActualizable {

    private static final int COLUMNA_ESTADO_RESERVA = 7;

    private final ClienteControlador clientes;
    private final HabitacionControlador habitaciones;
    private final ReservaControlador reservas;
    private final boolean administrador;

    private final JTextField documento = VistaUtil.campo("");
    private final JTextField nombre = VistaUtil.campo("");
    private final JTextField telefono = VistaUtil.campo("");
    private final JTextField documentoAdicional = VistaUtil.campo("");
    private final JTextField nombreAdicional = VistaUtil.campo("");
    private final JTextField telefonoAdicional = VistaUtil.campo("");
    private final JComboBox<ItemHabitacion> habitacion = new JComboBox<>();
    private final JTextField ingreso = VistaUtil.campo("");
    private final JTextField salida = VistaUtil.campo("");
    private final JTextField totalCalculado = VistaUtil.campoLectura("0.00");
    private final JTextField reservaCheckOut = VistaUtil.campoLectura("");

    private final JTextField busquedaClientes = VistaUtil.campo("");
    private final JTextField busquedaHabitaciones = VistaUtil.campo("");
    private final JComboBox<String> filtroEstadoReservas = new JComboBox<>();
    private final JTextField busquedaFinalizadas = VistaUtil.campo("");

    private final DefaultTableModel modeloClientes = VistaUtil.modeloTabla(
            new Object[]{"ID", "Nombre", "DNI", "Teléfono"},
            new Class<?>[]{Integer.class, String.class, String.class, String.class}
    );
    private final DefaultTableModel modeloHabitaciones = VistaUtil.modeloTabla(
            new Object[]{"ID", "Número", "Tipo", "Precio", "Camas"},
            new Class<?>[]{
                Integer.class, String.class, Object.class,
                BigDecimal.class, Integer.class
            }
    );
    private final DefaultTableModel modeloReservas = VistaUtil.modeloTabla(
            new Object[]{
                "ID",
                "Habitación",
                "Huéspedes",
                "Ingreso",
                "Salida",
                "Total",
                "Pagado",
                "Estado"
            },
            new Class<?>[]{
                Integer.class, Integer.class, String.class, String.class,
                String.class, BigDecimal.class, BigDecimal.class,
                EstadoReserva.class
            }
    );
    private final DefaultTableModel modeloHuespedesAdicionales
            = VistaUtil.modeloTabla(
                    new Object[]{"DNI", "Nombre", "Teléfono"},
                    new Class<?>[]{String.class, String.class, String.class}
            );
    private final DefaultTableModel modeloFinalizadas = VistaUtil.modeloTabla(
            new Object[]{
                "ID",
                "Habitación",
                "Huéspedes",
                "Ingreso",
                "Salida",
                "Total",
                "Pagado",
                "Estado"
            },
            new Class<?>[]{
                Integer.class, Integer.class, String.class, String.class,
                String.class, BigDecimal.class, BigDecimal.class,
                EstadoReserva.class
            }
    );

    private JTable tablaClientes;
    private JTable tablaHabitaciones;
    private JTable tablaReservas;
    private JTable tablaFinalizadas;
    private JTable tablaHuespedesAdicionales;
    private JButton botonRegistrar;
    private JButton botonCheckOut;
    private JButton botonCancelar;
    private JButton botonUsarPrincipal;
    private JButton botonAnadirAdicional;
    private List<Reserva> reservasCargadas = List.of();
    private Map<Integer, List<Cliente>> huespedesPorReserva = Map.of();
    private String documentoClienteCargado;
    private boolean cargandoCliente;

    public PanelRecepcion(
            ClienteControlador clientes,
            HabitacionControlador habitaciones,
            ReservaControlador reservas,
            boolean administrador
    ) {
        super(new BorderLayout(18, 18));
        this.clientes = Objects.requireNonNull(clientes);
        this.habitaciones = Objects.requireNonNull(habitaciones);
        this.reservas = Objects.requireNonNull(reservas);
        this.administrador = administrador;
        construir();
        limpiarCheckIn();
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
                "Recepción de huéspedes",
                "Registre ingresos, salidas y consulte reservas actuales",
                refrescar
        ), BorderLayout.NORTH);

        configurarActualizacionTotal();
        VistaUtil.alCambiarTexto(documento, () -> {
            if (!cargandoCliente
                    && documentoClienteCargado != null
                    && !documentoClienteCargado.equals(
                            documento.getText().trim()
                    )) {
                nombre.setText("");
                telefono.setText("");
                documentoClienteCargado = null;
            }
        });
        cargarFiltroEstados();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Check-in", construirCheckIn());
        tabs.addTab("Check-out", construirCheckOut());
        tabs.addTab("Reservas actuales", construirReservasActuales());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel construirCheckIn() {
        JScrollPane formularioScroll = new JScrollPane(
                construirFormularioCheckIn()
        );
        formularioScroll.setBorder(null);
        formularioScroll.getVerticalScrollBar().setUnitIncrement(16);
        JSplitPane division = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                formularioScroll,
                construirSeleccionCheckIn()
        );
        division.setResizeWeight(0.58);
        division.setBorder(null);
        division.setDividerLocation(620);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(division, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirFormularioCheckIn() {
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        agregarCampo(formulario, c, 0, 0, "DNI *", documento);
        agregarCampo(formulario, c, 1, 0, "Nombre completo *", nombre);
        agregarCampo(formulario, c, 2, 0, "Teléfono", telefono);
        agregarCampo(
                formulario,
                c,
                0,
                1,
                "Habitación disponible *",
                habitacion
        );
        agregarCampo(formulario, c, 1, 1, "Ingreso *", ingreso);
        agregarCampo(formulario, c, 2, 1, "Salida *", salida);
        agregarCampo(formulario, c, 3, 1, "Total calculado", totalCalculado);

        JPanel acciones = new JPanel(new GridLayout(1, 0, 8, 0));
        botonRegistrar = VistaUtil.botonPrimario("Registrar check-in");
        fijarAltoBoton(botonRegistrar);
        botonRegistrar.addActionListener(e -> VistaUtil.ejecutar(
                this,
                this::registrarCheckInAsync
        ));
        JButton limpiar = VistaUtil.botonSecundario("Limpiar formulario");
        fijarAltoBoton(limpiar);
        limpiar.addActionListener(e -> limpiarCheckIn());

        acciones.add(botonRegistrar);
        acciones.add(limpiar);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(formulario, BorderLayout.NORTH);
        panel.add(construirHuespedesAdicionales(), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(560, 540));
        return VistaUtil.seccion("Registro de huésped", panel);
    }

    private JPanel construirHuespedesAdicionales() {
        tablaHuespedesAdicionales = new JTable(modeloHuespedesAdicionales);
        VistaUtil.configurarTabla(
                tablaHuespedesAdicionales,
                modeloHuespedesAdicionales
        );

        JPanel campos = new JPanel(new GridLayout(0, 2, 8, 6));
        campos.add(new JLabel("DNI"));
        campos.add(documentoAdicional);
        campos.add(new JLabel("Nombre"));
        campos.add(nombreAdicional);
        campos.add(new JLabel("Teléfono"));
        campos.add(telefonoAdicional);

        JButton agregar = VistaUtil.botonSecundario("Añadir huésped");
        agregar.addActionListener(
                e -> VistaUtil.ejecutar(
                        this,
                        this::agregarHuespedAdicional
                )
        );
        JButton quitar = VistaUtil.botonPeligro("Quitar seleccionado");
        quitar.addActionListener(e -> quitarHuespedAdicional());

        JPanel acciones = new JPanel();
        acciones.add(agregar);
        acciones.add(quitar);

        JScrollPane scrollAcompanantes = new JScrollPane(
                tablaHuespedesAdicionales
        );
        scrollAcompanantes.setPreferredSize(new Dimension(0, 95));

        JPanel contenido = new JPanel(new BorderLayout(6, 6));
        contenido.add(campos, BorderLayout.NORTH);
        contenido.add(scrollAcompanantes, BorderLayout.CENTER);
        contenido.add(acciones, BorderLayout.SOUTH);
        return VistaUtil.seccion("Huéspedes adicionales", contenido);
    }

    private JPanel construirSeleccionCheckIn() {
        tablaHabitaciones = new JTable(modeloHabitaciones);
        VistaUtil.conectarBusqueda(
                busquedaHabitaciones,
                VistaUtil.configurarTabla(tablaHabitaciones, modeloHabitaciones)
        );
        tablaHabitaciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarHabitacionDesdeTabla();
            }
        });

        tablaClientes = new JTable(modeloClientes);
        VistaUtil.conectarBusquedaPorColumna(
                busquedaClientes,
                VistaUtil.configurarTabla(tablaClientes, modeloClientes),
                2
        );
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean seleccionado = tablaClientes.getSelectedRow() >= 0;
                botonUsarPrincipal.setEnabled(seleccionado);
                botonAnadirAdicional.setEnabled(seleccionado);
                if (seleccionado && documento.getText().isBlank()) {
                    usarClienteSeleccionadoComoPrincipal();
                }
            }
        });
        tablaClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2
                        && tablaClientes.getSelectedRow() >= 0) {
                    usarClienteSeleccionadoComoPrincipal();
                }
            }
        });

        JPanel habitacionesPanel = listadoConBusqueda(
                "Habitaciones disponibles",
                "Buscar habitación",
                busquedaHabitaciones,
                tablaHabitaciones
        );
        JPanel clientesPanel = listadoConBusqueda(
                "Huéspedes registrados",
                "Buscar por DNI",
                busquedaClientes,
                tablaClientes
        );
        botonUsarPrincipal = VistaUtil.botonPrimario(
                "Usar como principal"
        );
        botonUsarPrincipal.setEnabled(false);
        botonUsarPrincipal.addActionListener(
                e -> usarClienteSeleccionadoComoPrincipal()
        );
        botonAnadirAdicional = VistaUtil.botonSecundario(
                "Añadir como adicional"
        );
        botonAnadirAdicional.setEnabled(false);
        botonAnadirAdicional.addActionListener(e -> VistaUtil.ejecutar(
                this,
                this::agregarClienteSeleccionadoComoAdicional
        ));
        VistaUtil.fijarAltoBoton(botonUsarPrincipal, 34);
        VistaUtil.fijarAltoBoton(botonAnadirAdicional, 34);
        JPanel accionesClientes = new JPanel(new GridLayout(1, 0, 8, 0));
        accionesClientes.add(botonUsarPrincipal);
        accionesClientes.add(botonAnadirAdicional);
        clientesPanel.add(accionesClientes, BorderLayout.SOUTH);

        JSplitPane division = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                habitacionesPanel,
                clientesPanel
        );
        division.setResizeWeight(0.38);
        division.setBorder(null);
        division.setPreferredSize(new Dimension(390, 520));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(division, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirCheckOut() {
        tablaFinalizadas = new JTable(modeloFinalizadas);
        VistaUtil.conectarBusqueda(
                busquedaFinalizadas,
                VistaUtil.configurarTabla(tablaFinalizadas, modeloFinalizadas)
        );
        tablaFinalizadas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarReservaFinalizada();
            }
        });

        JPanel formulario = new JPanel();
        JLabel reservaLabel = new JLabel("Reserva ID");
        reservaLabel.setLabelFor(reservaCheckOut);
        formulario.add(reservaLabel);
        formulario.add(reservaCheckOut);

        botonCheckOut = VistaUtil.botonPrimario("Registrar check-out");
        botonCheckOut.setEnabled(false);
        botonCheckOut.addActionListener(e -> VistaUtil.ejecutar(
                this,
                this::registrarCheckOutAsync
        ));
        formulario.add(botonCheckOut);

        botonCancelar = VistaUtil.botonPeligro("Cancelar reserva");
        botonCancelar.setEnabled(false);
        botonCancelar.addActionListener(e -> VistaUtil.ejecutar(
                this,
                this::cancelarReservaAsync
        ));
        if (administrador) {
            formulario.add(botonCancelar);
        }

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.add(
                VistaUtil.seccion("Salida", formulario),
                BorderLayout.NORTH
        );
        panel.add(
                listadoConBusqueda(
                        "Reservas para check-out o cancelación",
                        "Buscar reserva",
                        busquedaFinalizadas,
                        tablaFinalizadas
                ),
                BorderLayout.CENTER
        );
        return panel;
    }

    private JPanel construirReservasActuales() {
        tablaReservas = new JTable(modeloReservas);
        VistaUtil.configurarTabla(tablaReservas, modeloReservas);
        filtroEstadoReservas.addActionListener(e -> cargarReservas());

        JPanel filtros = new JPanel();
        filtros.add(new JLabel("Estado"));
        filtros.add(filtroEstadoReservas);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(filtros, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);
        return VistaUtil.seccion("Reservas actuales", panel);
    }

    private JPanel listadoConBusqueda(
            String titulo,
            String etiqueta,
            JTextField busqueda,
            JTable tabla
    ) {
        JPanel filtros = new JPanel();
        JLabel label = new JLabel(etiqueta);
        label.setLabelFor(busqueda);
        filtros.add(label);
        filtros.add(busqueda);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(filtros, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return VistaUtil.seccion(titulo, panel);
    }

    private void agregarCampo(
            JPanel formulario,
            GridBagConstraints c,
            int fila,
            int columna,
            String etiqueta,
            java.awt.Component campo
    ) {
        int x = columna;
        c.gridy = fila * 2;
        c.gridx = x;
        JLabel label = new JLabel(etiqueta);
        label.setLabelFor(campo);
        formulario.add(label, c);

        c.gridy = fila * 2 + 1;
        c.gridx = x;
        formulario.add(campo, c);
    }

    private void fijarAltoBoton(JButton boton) {
        Dimension tamano = boton.getPreferredSize();
        tamano.height = Math.max(32, tamano.height);
        boton.setPreferredSize(tamano);
        boton.setMinimumSize(tamano);
    }

    private void agregarHuespedAdicional() {
        if (documentoAdicional.getText().isBlank()
                || nombreAdicional.getText().isBlank()) {
            throw new IllegalArgumentException(
                    "Ingrese DNI y nombre del huésped adicional"
            );
        }

        String dni = documentoAdicional.getText().trim();
        if (dni.equals(documento.getText().trim())) {
            throw new IllegalArgumentException(
                    "El huésped principal no puede repetirse como adicional"
            );
        }
        for (int fila = 0; fila < modeloHuespedesAdicionales.getRowCount();
                fila++) {
            if (dni.equals(String.valueOf(
                    modeloHuespedesAdicionales.getValueAt(fila, 0)
            ))) {
                throw new IllegalArgumentException(
                        "El DNI ya fue añadido a la reserva"
                );
            }
        }
        ItemHabitacion habitacionSeleccionada
                = (ItemHabitacion) habitacion.getSelectedItem();
        if (habitacionSeleccionada != null
                && modeloHuespedesAdicionales.getRowCount() + 2
                > habitacionSeleccionada.camas) {
            throw new IllegalArgumentException(
                    "La habitación admite como máximo "
                    + habitacionSeleccionada.camas + " huésped(es)"
            );
        }

        modeloHuespedesAdicionales.addRow(new Object[]{
            dni,
            nombreAdicional.getText().trim(),
            telefonoAdicional.getText().trim()
        });
        documentoAdicional.setText("");
        nombreAdicional.setText("");
        telefonoAdicional.setText("");
    }

    private void quitarHuespedAdicional() {
        int fila = tablaHuespedesAdicionales.getSelectedRow();
        if (fila < 0) {
            return;
        }

        modeloHuespedesAdicionales.removeRow(
                tablaHuespedesAdicionales.convertRowIndexToModel(fila)
        );
    }

    private void registrarCheckInAsync() {
        ItemHabitacion item = (ItemHabitacion) habitacion.getSelectedItem();
        if (item == null) {
            throw new IllegalArgumentException(
                    "No hay habitaciones disponibles para registrar check-in"
            );
        }

        String nombreValor = nombre.getText();
        String documentoValor = documento.getText();
        String telefonoValor = telefono.getText().isBlank()
                ? null : telefono.getText().trim();
        LocalDateTime fechaIngreso = VistaUtil.fecha(ingreso.getText());
        LocalDateTime fechaSalida = VistaUtil.fecha(salida.getText());
        List<DatosHuespedRecepcion> adicionales = huespedesAdicionales();
        VistaUtil.ejecutarAsync(
                this,
                botonRegistrar,
                () -> reservas.registrarRecepcion(
                        nombreValor,
                        documentoValor,
                        telefonoValor,
                        item.id,
                        fechaIngreso,
                        fechaSalida,
                        adicionales
                ),
                registrada -> {
                    limpiarCheckIn();
                    refrescarAsync();
                },
                "El check-in se registró correctamente"
        );
    }

    private void registrarCheckOutAsync() {
        int id = VistaUtil.entero(reservaCheckOut.getText());
        VistaUtil.ejecutarAsync(
                this,
                botonCheckOut,
                () -> reservas.registrarCheckOut(id),
                finalizada -> {
                    reservaCheckOut.setText("");
                    botonCheckOut.setEnabled(false);
                    botonCancelar.setEnabled(false);
                    refrescarAsync();
                },
                "El check-out se registró correctamente"
        );
    }

    private void cancelarReservaAsync() {
        if (!VistaUtil.confirmar(
                this,
                "¿Desea cancelar la reserva seleccionada? "
                + "La habitación pasará a limpieza.",
                "Confirmar cancelación"
        )) {
            return;
        }
        int id = VistaUtil.entero(reservaCheckOut.getText());
        VistaUtil.ejecutarAsync(
                this,
                botonCancelar,
                () -> reservas.cancelarRecepcion(id),
                cancelada -> {
                    reservaCheckOut.setText("");
                    botonCheckOut.setEnabled(false);
                    botonCancelar.setEnabled(false);
                    refrescarAsync();
                },
                "La reserva se canceló correctamente"
        );
    }

    @Override
    public void refrescarAsync() {
        VistaUtil.ejecutarAsync(
                this,
                null,
                () -> {
                    reservas.finalizarVencidas();
                    List<Reserva> listadoReservas = reservas.listar();
                    return new DatosRecepcion(
                            clientes.listar(),
                            habitaciones.listar(),
                            listadoReservas,
                            reservas.listarHuespedesPorReserva()
                    );
                },
                datos -> {
                    cargarClientes(datos.clientes());
                    cargarHabitacionesDisponibles(datos.habitaciones());
                    reservasCargadas = List.copyOf(datos.reservas());
                    huespedesPorReserva = datos.huespedesPorReserva();
                    cargarReservas();
                },
                null
        );
    }

    private void cargarClientes(List<Cliente> listado) {
        modeloClientes.setRowCount(0);
        for (Cliente cliente : listado) {
            modeloClientes.addRow(new Object[]{
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getTelefono() == null ? "" : cliente.getTelefono()
            });
        }
    }

    private void cargarHabitacionesDisponibles(List<Habitacion> listado) {
        habitacion.removeAllItems();
        modeloHabitaciones.setRowCount(0);
        for (Habitacion item : listado) {
            if (item.getEstado() == EstadoHabitacion.DISPONIBLE) {
                habitacion.addItem(new ItemHabitacion(
                        item.getId(),
                        item.getNumero(),
                        item.getPrecioPorNoche(),
                        item.getCantidadCamas()
                ));
                modeloHabitaciones.addRow(new Object[]{
                    item.getId(),
                    item.getNumero(),
                    item.getTipo(),
                    item.getPrecioPorNoche(),
                    item.getCantidadCamas()
                });
            }
        }
        actualizarTotalCalculado();
    }

    private void cargarReservas() {
        reservaCheckOut.setText("");
        if (botonCheckOut != null) {
            botonCheckOut.setEnabled(false);
            botonCancelar.setEnabled(false);
        }
        modeloReservas.setRowCount(0);
        modeloFinalizadas.setRowCount(0);
        for (Reserva reserva : reservasCargadas) {
            Object[] fila = filaReserva(reserva);
            if (coincideEstadoReserva(reserva)) {
                modeloReservas.addRow(fila);
            }
            if (reserva.getEstado() == EstadoReserva.FINALIZADA
                    || reserva.getEstado() == EstadoReserva.ACTIVA) {
                modeloFinalizadas.addRow(fila);
            }
        }
    }

    private boolean coincideEstadoReserva(Reserva reserva) {
        String seleccionado = String.valueOf(
                filtroEstadoReservas.getSelectedItem()
        );
        return seleccionado == null
                || seleccionado.equals("TODOS")
                || reserva.getEstado().name().equals(seleccionado);
    }

    private Object[] filaReserva(Reserva reserva) {
        return new Object[]{
            reserva.getId(),
            reserva.getHabitacionId(),
            formatoHuespedes(reserva),
            VistaUtil.FORMATO_FECHA.format(reserva.getFechaIngreso()),
            VistaUtil.FORMATO_FECHA.format(reserva.getFechaSalida()),
            reserva.getTotalHospedaje(),
            reserva.getMontoPagado(),
            reserva.getEstado()
        };
    }

    private String formatoHuespedes(Reserva reserva) {
        List<Cliente> huespedes = huespedesPorReserva.getOrDefault(
                reserva.getId(),
                List.of()
        );
        if (huespedes.isEmpty()) {
            return "Cliente ID " + reserva.getClienteId();
        }

        List<String> textos = new ArrayList<>();
        for (Cliente huesped : huespedes) {
            textos.add(
                    huesped.getNombreCompleto()
                    + " (DNI: "
                    + huesped.getDocumentoIdentidad()
                    + ")"
            );
        }
        return String.join("; ", textos);
    }

    private void seleccionarHabitacionDesdeTabla() {
        Integer id = VistaUtil.idFilaSeleccionada(tablaHabitaciones);
        if (id == null) {
            return;
        }

        for (int i = 0; i < habitacion.getItemCount(); i++) {
            ItemHabitacion item = habitacion.getItemAt(i);
            if (item.id == id) {
                habitacion.setSelectedIndex(i);
                return;
            }
        }
    }

    private void usarClienteSeleccionadoComoPrincipal() {
        ClienteTabla cliente = clienteSeleccionadoEnTabla();
        cargandoCliente = true;
        try {
            documento.setText(cliente.documento());
            nombre.setText(cliente.nombre());
            telefono.setText(cliente.telefono());
            documentoClienteCargado = cliente.documento();
        } finally {
            cargandoCliente = false;
        }
    }

    private void agregarClienteSeleccionadoComoAdicional() {
        ClienteTabla cliente = clienteSeleccionadoEnTabla();
        documentoAdicional.setText(cliente.documento());
        nombreAdicional.setText(cliente.nombre());
        telefonoAdicional.setText(cliente.telefono());
        agregarHuespedAdicional();
    }

    private ClienteTabla clienteSeleccionadoEnTabla() {
        int filaVista = tablaClientes.getSelectedRow();
        if (filaVista < 0) {
            throw new IllegalArgumentException(
                    "Seleccione un huésped de la tabla"
            );
        }
        int fila = tablaClientes.convertRowIndexToModel(filaVista);
        return new ClienteTabla(
                String.valueOf(modeloClientes.getValueAt(fila, 1)),
                String.valueOf(modeloClientes.getValueAt(fila, 2)),
                String.valueOf(modeloClientes.getValueAt(fila, 3))
        );
    }

    private void seleccionarReservaFinalizada() {
        Integer id = VistaUtil.idFilaSeleccionada(tablaFinalizadas);
        if (id != null) {
            reservaCheckOut.setText(String.valueOf(id));
            int fila = tablaFinalizadas.convertRowIndexToModel(
                    tablaFinalizadas.getSelectedRow()
            );
            EstadoReserva estado = (EstadoReserva) modeloFinalizadas.getValueAt(
                    fila,
                    COLUMNA_ESTADO_RESERVA
            );
            botonCheckOut.setEnabled(
                    estado == EstadoReserva.ACTIVA
                    || estado == EstadoReserva.FINALIZADA
            );
            botonCancelar.setEnabled(
                    administrador && estado == EstadoReserva.ACTIVA
            );
        }
    }

    private List<DatosHuespedRecepcion> huespedesAdicionales() {
        List<DatosHuespedRecepcion> resultado = new ArrayList<>();
        for (int fila = 0; fila < modeloHuespedesAdicionales.getRowCount();
                fila++) {
            String telefonoAdicional = String.valueOf(
                    modeloHuespedesAdicionales.getValueAt(fila, 2)
            );
            resultado.add(new DatosHuespedRecepcion(
                    String.valueOf(
                            modeloHuespedesAdicionales.getValueAt(fila, 1)
                    ),
                    String.valueOf(
                            modeloHuespedesAdicionales.getValueAt(fila, 0)
                    ),
                    telefonoAdicional.isBlank() ? null : telefonoAdicional
            ));
        }
        return resultado;
    }

    private void limpiarCheckIn() {
        documentoClienteCargado = null;
        documento.setText("");
        nombre.setText("");
        telefono.setText("");
        documentoAdicional.setText("");
        nombreAdicional.setText("");
        telefonoAdicional.setText("");
        modeloHuespedesAdicionales.setRowCount(0);
        if (tablaClientes != null) {
            tablaClientes.clearSelection();
        }
        if (botonUsarPrincipal != null) {
            botonUsarPrincipal.setEnabled(false);
            botonAnadirAdicional.setEnabled(false);
        }
        if (tablaHabitaciones != null) {
            tablaHabitaciones.clearSelection();
        }
        LocalDateTime ahora = LocalDateTime.now().withSecond(0).withNano(0);
        ingreso.setText(VistaUtil.FORMATO_FECHA.format(ahora));
        salida.setText(VistaUtil.FORMATO_FECHA.format(ahora.plusDays(1)));
        actualizarTotalCalculado();
    }

    private void cargarFiltroEstados() {
        filtroEstadoReservas.addItem("TODOS");
        for (EstadoReserva estado : EstadoReserva.values()) {
            filtroEstadoReservas.addItem(estado.name());
        }
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
            totalCalculado.setText(
                    reservas.calcularTotalHospedaje(
                            item.precioPorNoche,
                            fechaIngreso,
                            fechaSalida
                    ).toPlainString()
            );
        } catch (RuntimeException e) {
            totalCalculado.setText("Fecha inválida");
        }
    }

    private static final class ItemHabitacion {

        private final int id;
        private final String numero;
        private final BigDecimal precioPorNoche;
        private final int camas;

        private ItemHabitacion(
                int id,
                String numero,
                BigDecimal precioPorNoche,
                int camas
        ) {
            this.id = id;
            this.numero = numero;
            this.precioPorNoche = precioPorNoche;
            this.camas = camas;
        }

        @Override
        public String toString() {
            return numero + " - S/ " + precioPorNoche
                    + " - " + camas + " cama(s)";
        }
    }

    private record DatosRecepcion(
            List<Cliente> clientes,
            List<Habitacion> habitaciones,
            List<Reserva> reservas,
            Map<Integer, List<Cliente>> huespedesPorReserva
    ) {
    }

    private record ClienteTabla(
            String nombre,
            String documento,
            String telefono
    ) {
    }
}
