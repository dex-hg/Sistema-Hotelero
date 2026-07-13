package hotel.vista.panel;

import hotel.controlador.HabitacionControlador;

import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.TipoHabitacion;

import hotel.patrones.creacional.HabitacionBuilder;
import hotel.vista.VistaUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.List;
import java.util.Objects;

public final class PanelHabitaciones extends JPanel implements PanelActualizable {

    private static final String TODOS = "TODOS";

    private final HabitacionControlador habitaciones;
    private final boolean administrador;
    private final JComboBox<String> filtroEstado = new JComboBox<>();
    private final JComboBox<String> filtroTipo = new JComboBox<>();
    private final JComboBox<String> filtroCamas = new JComboBox<>();
    private final JComboBox<String> filtroBano = new JComboBox<>();
    private final JComboBox<String> filtroTv = new JComboBox<>();
    private final JTextField id = VistaUtil.campoLectura("");
    private final JTextField numero = VistaUtil.campo("");
    private final JComboBox<TipoHabitacion> tipo = new JComboBox<>(
            TipoHabitacion.values()
    );
    private final JTextField precio = VistaUtil.campo("0.00");
    private final JTextField camas = VistaUtil.campo("1");
    private final JComboBox<String> banoPrivado = new JComboBox<>(
            new String[]{"Sí", "No"}
    );
    private final JComboBox<String> tv = new JComboBox<>(
            new String[]{"Sí", "No"}
    );
    private final JLabel modoFormulario = new JLabel("Nueva habitación");
    private final JLabel estadoActual = new JLabel(
            "Seleccione una habitación"
    );
    private final JComboBox<EstadoHabitacion> nuevoEstado = new JComboBox<>();
    private JButton botonCrear;
    private JButton botonEliminar;
    private JButton botonCambiarEstado;
    private List<Habitacion> habitacionesCargadas = List.of();
    private final DefaultTableModel modelo = VistaUtil.modeloTabla(
            new Object[]{
                "ID",
                "Número",
                "Tipo",
                "Precio",
                "Camas",
                "Baño privado",
                "TV",
                "Estado"
            },
            new Class<?>[]{
                Integer.class, String.class, TipoHabitacion.class,
                java.math.BigDecimal.class, Integer.class, String.class,
                String.class, EstadoHabitacion.class
            }
    );
    private JTable tabla;

    public PanelHabitaciones(
            HabitacionControlador habitaciones,
            boolean administrador
    ) {
        super(new BorderLayout(18, 18));
        this.habitaciones = Objects.requireNonNull(habitaciones);
        this.administrador = administrador;
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
                "Catálogo y gestión de habitaciones",
                administrador
                        ? "Registre, actualice y consulte habitaciones"
                        : "Consulte disponibilidad y características",
                refrescar
        ), BorderLayout.NORTH);

        tabla = new JTable(modelo);
        VistaUtil.configurarTabla(tabla, modelo);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarHabitacion();
            }
        });

        cargarFiltros();
        filtroEstado.addActionListener(e -> aplicarFiltros());
        filtroTipo.addActionListener(e -> aplicarFiltros());
        filtroCamas.addActionListener(e -> aplicarFiltros());
        filtroBano.addActionListener(e -> aplicarFiltros());
        filtroTv.addActionListener(e -> aplicarFiltros());

        JPanel contenido = new JPanel(new BorderLayout(18, 18));
        contenido.add(
                administrador
                        ? construirFormularioDesplazable()
                        : construirPanelFiltrosLateral(),
                BorderLayout.WEST
        );
        contenido.add(construirListado(), BorderLayout.CENTER);
        add(contenido, BorderLayout.CENTER);
    }

    private JScrollPane construirFormularioDesplazable() {
        JScrollPane scroll = new JScrollPane(
                construirFormulario(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(390, 0));
        scroll.setMinimumSize(new Dimension(360, 0));
        return scroll;
    }

    private JPanel construirFormulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        agregarCampo(formulario, c, 0, "Número *", numero);
        agregarCampo(formulario, c, 1, "Tipo *", tipo);
        agregarCampo(formulario, c, 2, "Precio por noche *", precio);
        agregarCampo(formulario, c, 3, "Camas *", camas);
        agregarCampo(formulario, c, 4, "Baño privado", banoPrivado);
        agregarCampo(formulario, c, 5, "TV", tv);

        JPanel cabeceraFormulario = new JPanel(new BorderLayout(8, 8));
        cabeceraFormulario.add(modoFormulario, BorderLayout.CENTER);
        JButton nuevo = VistaUtil.botonCompacto("Nueva habitación");
        nuevo.addActionListener(e -> limpiarFormulario());
        cabeceraFormulario.add(nuevo, BorderLayout.EAST);

        JPanel acciones = new JPanel(new GridBagLayout());
        GridBagConstraints a = new GridBagConstraints();
        a.insets = new Insets(5, 0, 5, 0);
        a.fill = GridBagConstraints.HORIZONTAL;
        a.weightx = 1;
        a.gridx = 0;

        if (administrador) {
            botonCrear = VistaUtil.botonPrimario(
                    "Guardar nueva habitación"
            );
            VistaUtil.fijarAltoBoton(botonCrear, 34);
            botonCrear.addActionListener(e -> VistaUtil.ejecutar(
                    this,
                    this::guardarAsync
            ));

            botonEliminar = VistaUtil.botonPeligro("Eliminar habitación");
            VistaUtil.fijarAltoBoton(botonEliminar, 34);
            botonEliminar.setEnabled(false);
            botonEliminar.addActionListener(e -> VistaUtil.ejecutar(
                    this,
                    this::eliminarAsync
            ));
            agregarBoton(acciones, a, botonEliminar);
        }

        JButton limpiar = VistaUtil.botonSecundario("Limpiar formulario");
        VistaUtil.fijarAltoBoton(limpiar, 34);
        limpiar.addActionListener(e -> limpiarFormulario());
        agregarBoton(acciones, a, limpiar);

        JPanel datos = new JPanel(new BorderLayout(8, 8));
        datos.add(cabeceraFormulario, BorderLayout.NORTH);
        datos.add(formulario, BorderLayout.CENTER);
        if (botonCrear != null) {
            JPanel guardar = new JPanel(new BorderLayout());
            guardar.setBorder(
                    javax.swing.BorderFactory.createEmptyBorder(6, 6, 0, 6)
            );
            guardar.add(botonCrear, BorderLayout.CENTER);
            datos.add(guardar, BorderLayout.SOUTH);
        }

        JPanel panel = new JPanel(new BorderLayout(8, 12));
        panel.add(datos, BorderLayout.NORTH);
        panel.add(acciones, BorderLayout.CENTER);
        panel.add(construirGestionEstado(), BorderLayout.SOUTH);
        JPanel seccion = VistaUtil.seccion(
                administrador
                        ? "Gestión de habitación"
                        : "Consulta de habitación",
                panel
        );
        seccion.setPreferredSize(new Dimension(370, 610));
        seccion.setMinimumSize(new Dimension(350, 520));
        return seccion;
    }

    private JPanel construirListado() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        if (administrador) {
            panel.add(construirFiltros(), BorderLayout.NORTH);
        }
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return VistaUtil.seccion("Habitaciones registradas", panel);
    }

    private JPanel construirPanelFiltrosLateral() {
        JPanel panel = new JPanel(new BorderLayout(8, 16));
        panel.add(construirFiltrosVerticales(), BorderLayout.NORTH);
        panel.add(construirGestionEstado(), BorderLayout.CENTER);
        JPanel seccion = VistaUtil.seccion("Filtros y estado", panel);
        seccion.setPreferredSize(new Dimension(300, 0));
        seccion.setMinimumSize(new Dimension(280, 0));
        return seccion;
    }

    private JPanel construirGestionEstado() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.add(new JLabel("Estado actual"));
        panel.add(estadoActual);
        panel.add(new JLabel("Cambiar a"));
        nuevoEstado.setEnabled(false);
        panel.add(nuevoEstado);

        botonCambiarEstado = VistaUtil.botonSecundario("Aplicar estado");
        VistaUtil.fijarAltoBoton(botonCambiarEstado, 34);
        botonCambiarEstado.setEnabled(false);
        botonCambiarEstado.addActionListener(e -> VistaUtil.ejecutar(
                this,
                this::cambiarEstadoAsync
        ));
        panel.add(botonCambiarEstado);
        return VistaUtil.seccion("Estado operativo", panel);
    }

    private JPanel construirFiltros() {
        JPanel filtros = new JPanel();
        filtros.add(new JLabel("Estado"));
        filtros.add(filtroEstado);
        filtros.add(new JLabel("Tipo"));
        filtros.add(filtroTipo);
        filtros.add(new JLabel("Camas"));
        filtros.add(filtroCamas);
        filtros.add(new JLabel("Baño"));
        filtros.add(filtroBano);
        filtros.add(new JLabel("TV"));
        filtros.add(filtroTv);
        return filtros;
    }

    private JPanel construirFiltrosVerticales() {
        JPanel filtros = new JPanel(new GridLayout(0, 1, 0, 8));
        filtros.add(new JLabel("Estado"));
        filtros.add(filtroEstado);
        filtros.add(new JLabel("Tipo"));
        filtros.add(filtroTipo);
        filtros.add(new JLabel("Camas"));
        filtros.add(filtroCamas);
        filtros.add(new JLabel("Baño"));
        filtros.add(filtroBano);
        filtros.add(new JLabel("TV"));
        filtros.add(filtroTv);
        return filtros;
    }

    private void agregarCampo(
            JPanel formulario,
            GridBagConstraints c,
            int fila,
            String etiqueta,
            java.awt.Component campo
    ) {
        c.gridy = fila;
        c.gridx = 0;
        c.weightx = 0;
        JLabel label = new JLabel(etiqueta);
        label.setLabelFor(campo);
        formulario.add(label, c);

        c.gridx = 1;
        c.weightx = 1;
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

    private void cargarFiltros() {
        filtroEstado.addItem(TODOS);
        for (EstadoHabitacion estado : EstadoHabitacion.values()) {
            filtroEstado.addItem(estado.name());
        }

        filtroTipo.addItem(TODOS);
        for (TipoHabitacion item : TipoHabitacion.values()) {
            filtroTipo.addItem(item.name());
        }

        filtroCamas.addItem(TODOS);
        for (int i = 1; i <= 8; i++) {
            filtroCamas.addItem(String.valueOf(i));
        }

        filtroBano.addItem(TODOS);
        filtroBano.addItem("Sí");
        filtroBano.addItem("No");

        filtroTv.addItem(TODOS);
        filtroTv.addItem("Sí");
        filtroTv.addItem("No");
    }

    @Override
    public void refrescarAsync() {
        VistaUtil.ejecutarAsync(
                this,
                null,
                habitaciones::listar,
                listado -> {
                    habitacionesCargadas = List.copyOf(listado);
                    aplicarFiltros();
                    limpiarFormulario();
                },
                null
        );
    }

    private void aplicarFiltros() {
        modelo.setRowCount(0);
        for (Habitacion habitacion : habitacionesCargadas) {
            if (!coincideEstado(habitacion)
                    || !coincideTipo(habitacion)
                    || !coincideCamas(habitacion)
                    || !coincideBano(habitacion)
                    || !coincideTv(habitacion)) {
                continue;
            }

            modelo.addRow(new Object[]{
                habitacion.getId(),
                habitacion.getNumero(),
                habitacion.getTipo(),
                habitacion.getPrecioPorNoche(),
                habitacion.getCantidadCamas(),
                habitacion.tieneBanoPrivado() ? "Sí" : "No",
                habitacion.tieneTv() ? "Sí" : "No",
                habitacion.getEstado()
            });
        }
    }

    private boolean coincideEstado(Habitacion habitacion) {
        String seleccionado = String.valueOf(
                filtroEstado.getSelectedItem()
        );
        return TODOS.equals(seleccionado)
                || habitacion.getEstado().name().equals(seleccionado);
    }

    private boolean coincideTipo(Habitacion habitacion) {
        String seleccionado = String.valueOf(
                filtroTipo.getSelectedItem()
        );
        return TODOS.equals(seleccionado)
                || habitacion.getTipo().name().equals(seleccionado);
    }

    private boolean coincideCamas(Habitacion habitacion) {
        String seleccionado = String.valueOf(filtroCamas.getSelectedItem());
        return TODOS.equals(seleccionado)
                || habitacion.getCantidadCamas()
                == VistaUtil.entero(seleccionado);
    }

    private boolean coincideBano(Habitacion habitacion) {
        String seleccionado = String.valueOf(filtroBano.getSelectedItem());
        return TODOS.equals(seleccionado)
                || habitacion.tieneBanoPrivado() == "Sí".equals(seleccionado);
    }

    private boolean coincideTv(Habitacion habitacion) {
        String seleccionado = String.valueOf(filtroTv.getSelectedItem());
        return TODOS.equals(seleccionado)
                || habitacion.tieneTv() == "Sí".equals(seleccionado);
    }

    private void seleccionarHabitacion() {
        Integer seleccionado = VistaUtil.idFilaSeleccionada(tabla);
        if (seleccionado == null) {
            return;
        }

        int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
        EstadoHabitacion estado = (EstadoHabitacion) modelo.getValueAt(
                fila,
                7
        );
        cargarTransicionesEstado(estado);
        if (!administrador) {
            return;
        }
        id.setText(String.valueOf(modelo.getValueAt(fila, 0)));
        numero.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        tipo.setSelectedItem(modelo.getValueAt(fila, 2));
        precio.setText(String.valueOf(modelo.getValueAt(fila, 3)));
        camas.setText(String.valueOf(modelo.getValueAt(fila, 4)));
        banoPrivado.setSelectedItem(
                modelo.getValueAt(fila, 5)
        );
        tv.setSelectedItem(modelo.getValueAt(fila, 6));
        modoFormulario.setText(
                "Editando habitación " + modelo.getValueAt(fila, 1)
        );
        if (botonCrear != null) {
            botonCrear.setText("Guardar cambios");
            botonCrear.setEnabled(true);
            botonEliminar.setEnabled(true);
        }
    }

    private void cargarTransicionesEstado(EstadoHabitacion actual) {
        estadoActual.setText(VistaUtil.textoEnum(actual));
        nuevoEstado.removeAllItems();

        switch (actual) {
            case EN_LIMPIEZA -> {
                nuevoEstado.addItem(EstadoHabitacion.DISPONIBLE);
                if (administrador) {
                    nuevoEstado.addItem(EstadoHabitacion.MANTENIMIENTO);
                }
            }
            case DISPONIBLE -> {
                if (administrador) {
                    nuevoEstado.addItem(EstadoHabitacion.MANTENIMIENTO);
                }
            }
            case MANTENIMIENTO -> {
                if (administrador) {
                    nuevoEstado.addItem(EstadoHabitacion.EN_LIMPIEZA);
                }
            }
            case OCUPADA -> {
                // El término de la reserva controla esta transición.
            }
        }

        boolean permitido = nuevoEstado.getItemCount() > 0;
        nuevoEstado.setEnabled(permitido);
        botonCambiarEstado.setEnabled(permitido);
    }

    private void cambiarEstadoAsync() {
        Integer habitacionId = VistaUtil.idFilaSeleccionada(tabla);
        EstadoHabitacion destino
                = (EstadoHabitacion) nuevoEstado.getSelectedItem();
        if (habitacionId == null || destino == null) {
            throw new IllegalArgumentException(
                    "Seleccione una habitación y un nuevo estado"
            );
        }
        if (destino == EstadoHabitacion.MANTENIMIENTO
                && !VistaUtil.confirmar(
                        this,
                        "¿Desea enviar la habitación seleccionada a mantenimiento?",
                        "Confirmar mantenimiento"
                )) {
            return;
        }

        VistaUtil.ejecutarAsync(
                this,
                botonCambiarEstado,
                () -> switch (destino) {
                    case DISPONIBLE -> habitaciones.habilitar(habitacionId);
                    case EN_LIMPIEZA ->
                        habitaciones.iniciarLimpieza(habitacionId);
                    case MANTENIMIENTO ->
                        habitaciones.enviarAMantenimiento(habitacionId);
                    case OCUPADA -> throw new IllegalArgumentException(
                    "El estado OCUPADA sólo se asigna mediante check-in"
                    );
                },
                actualizada -> {
                    limpiarFormulario();
                    refrescarAsync();
                },
                "El estado de la habitación se actualizó correctamente"
        );
    }

    private void crearAsync() {
        String numeroValor = numero.getText();
        TipoHabitacion tipoValor = (TipoHabitacion) tipo.getSelectedItem();
        java.math.BigDecimal precioValor = VistaUtil.decimal(precio.getText());
        int camasValor = VistaUtil.entero(camas.getText());
        boolean banoValor = "Sí".equals(banoPrivado.getSelectedItem());
        boolean tvValor = "Sí".equals(tv.getSelectedItem());
        VistaUtil.ejecutarAsync(
                this,
                botonCrear,
                () -> habitaciones.crear(
                        numeroValor, tipoValor, precioValor, camasValor,
                        banoValor, tvValor
                ),
                creada -> {
                    limpiarFormulario();
                    refrescarAsync();
                },
                "La habitación se registró correctamente"
        );
    }

    private void guardarAsync() {
        if (id.getText().isBlank()) {
            crearAsync();
        } else {
            actualizarAsync();
        }
    }

    private void actualizarAsync() {
        int habitacionId = VistaUtil.entero(id.getText());
        String numeroValor = numero.getText();
        TipoHabitacion tipoValor = (TipoHabitacion) tipo.getSelectedItem();
        java.math.BigDecimal precioValor = VistaUtil.decimal(precio.getText());
        int camasValor = VistaUtil.entero(camas.getText());
        boolean banoValor = "Sí".equals(banoPrivado.getSelectedItem());
        boolean tvValor = "Sí".equals(tv.getSelectedItem());
        VistaUtil.ejecutarAsync(
                this,
                botonCrear,
                () -> {
                    Habitacion actual = habitaciones.buscarPorId(habitacionId);
                    HabitacionBuilder builder = new HabitacionBuilder()
                            .conId(actual.getId())
                            .paraHotel(actual.getHotelId())
                            .conNumero(numeroValor)
                            .deTipo(tipoValor)
                            .conPrecioPorNoche(precioValor)
                            .conCantidadCamas(camasValor)
                            .conEstado(actual.getEstado());
                    if (banoValor) {
                        builder.conBanoPrivado();
                    }
                    if (tvValor) {
                        builder.conTv();
                    }
                    boolean actualizado = habitaciones.actualizar(
                            builder.construir()
                    );
                    VistaUtil.exigirExito(
                            actualizado,
                            "La habitación ya no existe"
                    );
                    return actualizado;
                },
                actualizado -> {
                    limpiarFormulario();
                    refrescarAsync();
                },
                "La habitación se actualizó correctamente"
        );
    }

    private void eliminarAsync() {
        if (!VistaUtil.confirmar(
                this,
                "¿Desea eliminar la habitación seleccionada?",
                "Confirmar eliminación"
        )) {
            return;
        }
        int habitacionId = VistaUtil.entero(id.getText());
        VistaUtil.ejecutarAsync(
                this,
                botonEliminar,
                () -> {
                    boolean eliminado = habitaciones.eliminar(habitacionId);
                    VistaUtil.exigirExito(
                            eliminado,
                            "La habitación ya no existe"
                    );
                    return eliminado;
                },
                eliminado -> {
                    limpiarFormulario();
                    refrescarAsync();
                },
                "La habitación se eliminó correctamente"
        );
    }

    private void limpiarFormulario() {
        id.setText("");
        numero.setText("");
        tipo.setSelectedIndex(0);
        precio.setText("0.00");
        camas.setText("1");
        banoPrivado.setSelectedItem("No");
        tv.setSelectedItem("No");
        tabla.clearSelection();
        modoFormulario.setText("Nueva habitación");
        estadoActual.setText("Seleccione una habitación");
        nuevoEstado.removeAllItems();
        nuevoEstado.setEnabled(false);
        if (botonCambiarEstado != null) {
            botonCambiarEstado.setEnabled(false);
        }
        if (botonCrear != null) {
            botonCrear.setText("Guardar nueva habitación");
            botonCrear.setEnabled(true);
            botonEliminar.setEnabled(false);
        }
    }
}
