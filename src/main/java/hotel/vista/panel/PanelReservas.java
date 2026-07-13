package hotel.vista.panel;

import hotel.controlador.ClienteControlador;
import hotel.controlador.HabitacionControlador;
import hotel.controlador.ReservaControlador;
import hotel.modelo.entidades.Cliente;
import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.vista.VistaUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PanelReservas extends JPanel implements PanelActualizable {

    private final HabitacionControlador habitaciones;
    private final ClienteControlador clientes;
    private final ReservaControlador reservas;

    private final JComboBox<ItemHabitacion> habitacion = new JComboBox<>();
    private final JComboBox<ItemCliente> cliente = new JComboBox<>();
    private final JTextField ingreso = VistaUtil.campo("");
    private final JTextField salida = VistaUtil.campo("");
    private final JTextField totalCalculado = VistaUtil.campoLectura("0.00");
    private final JTextField busqueda = VistaUtil.campo("");
    private final JComboBox<String> filtroEstado = new JComboBox<>();

    private final DefaultTableModel modelo = VistaUtil.modeloTabla(
            new Object[]{
                "ID", "Habitación", "Huésped", "Ingreso",
                "Salida", "Total", "Estado"
            },
            new Class<?>[]{
                Integer.class, String.class, String.class, String.class,
                String.class, BigDecimal.class, EstadoReserva.class
            }
    );

    private JTable tabla;
    private JButton botonCrear;
    private JButton botonCheckIn;
    private JButton botonCheckOut;
    private JButton botonCancelar;
    private Integer reservaSeleccionada;
    private List<Reserva> reservasCargadas = List.of();
    private Map<Integer, Habitacion> habitacionesPorId = Map.of();
    private Map<Integer, Cliente> clientesPorId = Map.of();

    public PanelReservas(
            HabitacionControlador habitaciones,
            ClienteControlador clientes,
            ReservaControlador reservas
    ) {
        super(new BorderLayout(18, 18));
        this.habitaciones = Objects.requireNonNull(habitaciones);
        this.clientes = Objects.requireNonNull(clientes);
        this.reservas = Objects.requireNonNull(reservas);
        construir();
        limpiarReserva();
        refrescarAsync();
    }

    private void construir() {
        JButton refrescar = VistaUtil.botonCompacto("Actualizar datos");
        refrescar.addActionListener(e -> refrescarAsync());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));
        add(VistaUtil.encabezadoModulo(
                "Gestión administrativa de reservas",
                "Registre reservas y controle su ciclo de vida",
                refrescar
        ), BorderLayout.NORTH);

        tabla = new JTable(modelo);
        VistaUtil.conectarBusqueda(
                busqueda,
                VistaUtil.configurarTabla(tabla, modelo)
        );
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarReserva();
            }
        });

        filtroEstado.addItem("TODOS");
        for (EstadoReserva estado : EstadoReserva.values()) {
            filtroEstado.addItem(estado.name());
        }
        filtroEstado.addActionListener(e -> cargarTabla());

        JSplitPane division = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                construirFormulario(),
                construirListado()
        );
        division.setResizeWeight(0.34);
        division.setDividerLocation(390);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);
    }

    private JPanel construirFormulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        agregarCampo(formulario, c, 0, "Habitación disponible *", habitacion);
        agregarCampo(formulario, c, 1, "Huésped principal *", cliente);
        agregarCampo(formulario, c, 2, "Ingreso *", ingreso);
        agregarCampo(formulario, c, 3, "Salida *", salida);
        agregarCampo(formulario, c, 4, "Total calculado", totalCalculado);

        habitacion.addActionListener(e -> actualizarTotal());
        VistaUtil.alCambiarTexto(ingreso, this::actualizarTotal);
        VistaUtil.alCambiarTexto(salida, this::actualizarTotal);

        botonCrear = VistaUtil.botonPrimario("Registrar reserva");
        botonCrear.addActionListener(e -> VistaUtil.ejecutar(
                this,
                this::crearAsync
        ));
        JButton limpiar = VistaUtil.botonSecundario("Limpiar formulario");
        limpiar.addActionListener(e -> limpiarReserva());

        JPanel accionesReserva = new JPanel(new GridLayout(0, 1, 0, 8));
        accionesReserva.add(botonCrear);
        accionesReserva.add(limpiar);

        JPanel nueva = new JPanel(new BorderLayout(8, 8));
        nueva.add(formulario, BorderLayout.NORTH);
        nueva.add(accionesReserva, BorderLayout.CENTER);

        botonCheckIn = VistaUtil.botonSecundario("Registrar check-in");
        VistaUtil.fijarAltoBoton(botonCheckIn, 34);
        botonCheckIn.addActionListener(e -> VistaUtil.ejecutar(this, () -> ejecutarCiclo(
                botonCheckIn,
                reservas::registrarCheckIn,
                "El check-in se registró correctamente",
                false
        )));
        botonCheckOut = VistaUtil.botonPrimario("Registrar check-out");
        VistaUtil.fijarAltoBoton(botonCheckOut, 34);
        botonCheckOut.addActionListener(e -> VistaUtil.ejecutar(this, () -> ejecutarCiclo(
                botonCheckOut,
                reservas::registrarCheckOut,
                "El check-out se registró correctamente",
                false
        )));
        botonCancelar = VistaUtil.botonPeligro("Cancelar reserva");
        VistaUtil.fijarAltoBoton(botonCancelar, 34);
        botonCancelar.addActionListener(e -> VistaUtil.ejecutar(this, () -> ejecutarCiclo(
                botonCancelar,
                this::cancelarAdministrativa,
                "La reserva se canceló correctamente",
                true
        )));

        JPanel ciclo = new JPanel(new GridBagLayout());
        GridBagConstraints acciones = new GridBagConstraints();
        acciones.gridx = 0;
        acciones.gridy = 0;
        acciones.weightx = 1;
        acciones.fill = GridBagConstraints.HORIZONTAL;
        acciones.insets = new Insets(0, 0, 8, 0);
        ciclo.add(new JLabel(
                "Seleccione una reserva para habilitar acciones"
        ), acciones);
        acciones.gridy++;
        ciclo.add(botonCheckIn, acciones);
        acciones.gridy++;
        ciclo.add(botonCheckOut, acciones);
        acciones.gridy++;
        acciones.insets = new Insets(0, 0, 0, 0);
        ciclo.add(botonCancelar, acciones);
        acciones.gridy++;
        acciones.weighty = 1;
        acciones.fill = GridBagConstraints.BOTH;
        ciclo.add(new JPanel(), acciones);
        deshabilitarAcciones();

        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.add(VistaUtil.seccion("Nueva reserva", nueva), BorderLayout.NORTH);
        contenido.add(VistaUtil.seccion("Ciclo de vida", ciclo), BorderLayout.CENTER);
        contenido.setMinimumSize(new Dimension(340, 0));
        return contenido;
    }

    private JPanel construirListado() {
        JPanel filtros = new JPanel();
        JLabel buscarLabel = new JLabel("Buscar");
        buscarLabel.setLabelFor(busqueda);
        filtros.add(buscarLabel);
        filtros.add(busqueda);
        JLabel estadoLabel = new JLabel("Estado");
        estadoLabel.setLabelFor(filtroEstado);
        filtros.add(estadoLabel);
        filtros.add(filtroEstado);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(filtros, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return VistaUtil.seccion("Reservas registradas", panel);
    }

    private void agregarCampo(
            JPanel formulario,
            GridBagConstraints c,
            int fila,
            String texto,
            java.awt.Component campo
    ) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setLabelFor(campo);
        c.gridx = 0;
        c.gridy = fila * 2;
        formulario.add(etiqueta, c);
        c.gridy++;
        formulario.add(campo, c);
    }

    @Override
    public void refrescarAsync() {
        VistaUtil.ejecutarAsync(
                this,
                null,
                () -> {
                    List<Reserva> listadoReservas = reservas.listar();
                    return new DatosPanel(
                            habitaciones.listar(),
                            clientes.listar(),
                            listadoReservas
                    );
                },
                this::aplicarDatos,
                null
        );
    }

    private void aplicarDatos(DatosPanel datos) {
        habitacionesPorId = datos.habitaciones().stream().collect(
                Collectors.toMap(Habitacion::getId, item -> item)
        );
        clientesPorId = datos.clientes().stream().collect(
                Collectors.toMap(Cliente::getId, item -> item)
        );
        reservasCargadas = List.copyOf(datos.reservas());

        habitacion.removeAllItems();
        for (Habitacion item : datos.habitaciones()) {
            if (item.getEstado() == EstadoHabitacion.DISPONIBLE) {
                habitacion.addItem(new ItemHabitacion(item));
            }
        }
        cliente.removeAllItems();
        for (Cliente item : datos.clientes()) {
            cliente.addItem(new ItemCliente(item));
        }
        cargarTabla();
        deshabilitarAcciones();
        actualizarTotal();
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        String estado = String.valueOf(filtroEstado.getSelectedItem());
        for (Reserva reserva : reservasCargadas) {
            if (!"TODOS".equals(estado)
                    && !reserva.getEstado().name().equals(estado)) {
                continue;
            }
            Habitacion habitacionItem = habitacionesPorId.get(
                    reserva.getHabitacionId()
            );
            Cliente clienteItem = clientesPorId.get(reserva.getClienteId());
            modelo.addRow(new Object[]{
                reserva.getId(),
                habitacionItem == null
                        ? String.valueOf(reserva.getHabitacionId())
                        : habitacionItem.getNumero(),
                clienteItem == null
                        ? String.valueOf(reserva.getClienteId())
                        : clienteItem.getNombreCompleto(),
                VistaUtil.FORMATO_FECHA.format(reserva.getFechaIngreso()),
                VistaUtil.FORMATO_FECHA.format(reserva.getFechaSalida()),
                reserva.getTotalPagado(),
                reserva.getEstado()
            });
        }
    }

    private void seleccionarReserva() {
        reservaSeleccionada = VistaUtil.idFilaSeleccionada(tabla);
        if (reservaSeleccionada == null) {
            deshabilitarAcciones();
            return;
        }

        Reserva reserva = reservasCargadas.stream()
                .filter(item -> item.getId().equals(reservaSeleccionada))
                .findFirst()
                .orElse(null);
        if (reserva == null) {
            deshabilitarAcciones();
            return;
        }

        Habitacion habitacionReserva = habitacionesPorId.get(
                reserva.getHabitacionId()
        );
        EstadoHabitacion estadoHabitacion = habitacionReserva == null
                ? null : habitacionReserva.getEstado();
        boolean activa = reserva.getEstado() == EstadoReserva.ACTIVA;
        boolean finalizada = reserva.getEstado() == EstadoReserva.FINALIZADA;
        botonCheckIn.setEnabled(
                activa && estadoHabitacion == EstadoHabitacion.DISPONIBLE
        );
        botonCheckOut.setEnabled(
                finalizada && (estadoHabitacion == EstadoHabitacion.OCUPADA
                || estadoHabitacion == EstadoHabitacion.EN_LIMPIEZA)
        );
        botonCancelar.setEnabled(activa);
    }

    private void crearAsync() {
        ItemHabitacion habitacionItem
                = (ItemHabitacion) habitacion.getSelectedItem();
        ItemCliente clienteItem = (ItemCliente) cliente.getSelectedItem();
        if (habitacionItem == null || clienteItem == null) {
            throw new IllegalArgumentException(
                    "Seleccione una habitación y un huésped"
            );
        }
        LocalDateTime fechaIngreso = VistaUtil.fecha(ingreso.getText());
        LocalDateTime fechaSalida = VistaUtil.fecha(salida.getText());
        BigDecimal total = calcularTotal(
                habitacionItem.precio(),
                fechaIngreso,
                fechaSalida
        );
        VistaUtil.ejecutarAsync(
                this,
                botonCrear,
                () -> reservas.crear(
                        habitacionItem.id(),
                        clienteItem.id(),
                        fechaIngreso,
                        fechaSalida,
                        total
                ),
                creada -> {
                    limpiarReserva();
                    refrescarAsync();
                },
                "La reserva se registró correctamente"
        );
    }

    private void ejecutarCiclo(
            JButton boton,
            java.util.function.IntFunction<Reserva> accion,
            String mensaje,
            boolean confirmar
    ) {
        if (reservaSeleccionada == null) {
            throw new IllegalArgumentException("Seleccione una reserva");
        }
        if (confirmar && !VistaUtil.confirmar(
                this,
                "¿Desea cancelar la reserva seleccionada?",
                "Confirmar cancelación"
        )) {
            return;
        }
        int id = reservaSeleccionada;
        VistaUtil.ejecutarAsync(
                this,
                boton,
                () -> accion.apply(id),
                resultado -> {
                    deshabilitarAcciones();
                    refrescarAsync();
                },
                mensaje
        );
    }

    private Reserva cancelarAdministrativa(int reservaId) {
        Reserva reserva = reservasCargadas.stream()
                .filter(item -> item.getId().equals(reservaId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                "La reserva seleccionada ya no está disponible"
        ));
        Habitacion habitacionReserva = habitacionesPorId.get(
                reserva.getHabitacionId()
        );
        if (habitacionReserva != null
                && habitacionReserva.getEstado() == EstadoHabitacion.OCUPADA) {
            return reservas.cancelarRecepcion(reservaId);
        }
        return reservas.cancelar(reservaId);
    }

    private void limpiarReserva() {
        LocalDateTime ahora = LocalDateTime.now().withSecond(0).withNano(0);
        ingreso.setText(VistaUtil.FORMATO_FECHA.format(ahora));
        salida.setText(VistaUtil.FORMATO_FECHA.format(ahora.plusDays(1)));
        if (habitacion.getItemCount() > 0) {
            habitacion.setSelectedIndex(0);
        }
        if (cliente.getItemCount() > 0) {
            cliente.setSelectedIndex(0);
        }
        actualizarTotal();
    }

    private void actualizarTotal() {
        ItemHabitacion item = (ItemHabitacion) habitacion.getSelectedItem();
        if (item == null) {
            totalCalculado.setText("0.00");
            return;
        }
        try {
            totalCalculado.setText(calcularTotal(
                    item.precio(),
                    VistaUtil.fecha(ingreso.getText()),
                    VistaUtil.fecha(salida.getText())
            ).toPlainString());
        } catch (RuntimeException e) {
            totalCalculado.setText("Fecha inválida");
        }
    }

    private BigDecimal calcularTotal(
            BigDecimal precio,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida
    ) {
        if (!fechaSalida.isAfter(fechaIngreso)) {
            throw new IllegalArgumentException(
                    "La salida debe ser posterior al ingreso"
            );
        }
        long dias = ChronoUnit.DAYS.between(
                fechaIngreso.toLocalDate(),
                fechaSalida.toLocalDate()
        );
        return precio.multiply(BigDecimal.valueOf(Math.max(1, dias)));
    }

    private void deshabilitarAcciones() {
        reservaSeleccionada = null;
        if (tabla != null) {
            tabla.clearSelection();
        }
        botonCheckIn.setEnabled(false);
        botonCheckOut.setEnabled(false);
        botonCancelar.setEnabled(false);
    }

    private record DatosPanel(
            List<Habitacion> habitaciones,
            List<Cliente> clientes,
            List<Reserva> reservas
    ) {
    }

    private record ItemHabitacion(
            int id,
            String numero,
            BigDecimal precio,
            int camas
    ) {
        private ItemHabitacion(Habitacion habitacion) {
            this(
                    habitacion.getId(),
                    habitacion.getNumero(),
                    habitacion.getPrecioPorNoche(),
                    habitacion.getCantidadCamas()
            );
        }

        @Override
        public String toString() {
            return numero + " - S/ " + precio + " - " + camas + " cama(s)";
        }
    }

    private record ItemCliente(int id, String nombre, String documento) {
        private ItemCliente(Cliente cliente) {
            this(
                    cliente.getId(),
                    cliente.getNombreCompleto(),
                    cliente.getDocumentoIdentidad()
            );
        }

        @Override
        public String toString() {
            return nombre + " - DNI " + documento;
        }
    }
}
