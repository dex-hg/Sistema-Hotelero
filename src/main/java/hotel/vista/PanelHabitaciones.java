package hotel.vista;

import hotel.controlador.HabitacionControlador;
import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.TipoHabitacion;
import hotel.patrones.creacional.HabitacionBuilder;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;

import java.util.List;
import java.util.Objects;

public final class PanelHabitaciones extends JPanel {

    private static final String TODOS = "TODOS";

    private final HabitacionControlador habitaciones;
    private final boolean administrador;
    private final JComboBox<String> filtroEstado = new JComboBox<>();
    private final JComboBox<String> filtroTipo = new JComboBox<>();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{
                "ID", "Numero", "Tipo", "Precio", "Camas",
                "Bano privado", "TV", "Estado"
            },
            0
    );

    public PanelHabitaciones(
            HabitacionControlador habitaciones,
            boolean administrador
    ) {
        super(new BorderLayout(10, 10));
        this.habitaciones = Objects.requireNonNull(habitaciones);
        this.administrador = administrador;
        construir();
        refrescar();
    }

    private void construir() {
        JTable tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);

        cargarFiltros();

        JButton refrescar = new JButton("Refrescar");
        refrescar.addActionListener(e -> VistaUtil.ejecutar(this, this::refrescar));

        filtroEstado.addActionListener(e -> VistaUtil.ejecutar(this, this::refrescar));
        filtroTipo.addActionListener(e -> VistaUtil.ejecutar(this, this::refrescar));

        JPanel acciones = new JPanel();
        acciones.add(new javax.swing.JLabel("Estado"));
        acciones.add(filtroEstado);
        acciones.add(new javax.swing.JLabel("Tipo"));
        acciones.add(filtroTipo);
        acciones.add(refrescar);

        if (administrador) {
            JButton crear = new JButton("Crear");
            crear.addActionListener(e -> VistaUtil.ejecutar(this, this::crear));

            JButton modificar = new JButton("Modificar");
            modificar.addActionListener(e -> VistaUtil.ejecutar(this, this::modificar));

            JButton eliminar = new JButton("Eliminar");
            eliminar.addActionListener(e -> VistaUtil.ejecutar(this, this::eliminar));

            acciones.add(crear);
            acciones.add(modificar);
            acciones.add(eliminar);
        }

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(acciones, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    private void cargarFiltros() {
        filtroEstado.addItem(TODOS);
        for (EstadoHabitacion estado : EstadoHabitacion.values()) {
            filtroEstado.addItem(estado.name());
        }

        filtroTipo.addItem(TODOS);
        for (TipoHabitacion tipo : TipoHabitacion.values()) {
            filtroTipo.addItem(tipo.name());
        }
    }

    private void refrescar() {
        modelo.setRowCount(0);
        List<Habitacion> listado = habitaciones.listar();
        for (Habitacion habitacion : listado) {
            if (!coincideEstado(habitacion) || !coincideTipo(habitacion)) {
                continue;
            }

            modelo.addRow(new Object[]{
                habitacion.getId(),
                habitacion.getNumero(),
                habitacion.getTipo(),
                habitacion.getPrecioPorNoche(),
                habitacion.getCantidadCamas(),
                habitacion.tieneBanoPrivado() ? "Si" : "No",
                habitacion.tieneTv() ? "Si" : "No",
                habitacion.getEstado()
            });
        }
    }

    private boolean coincideEstado(Habitacion habitacion) {
        String seleccionado = String.valueOf(filtroEstado.getSelectedItem());
        return TODOS.equals(seleccionado)
                || habitacion.getEstado().name().equals(seleccionado);
    }

    private boolean coincideTipo(Habitacion habitacion) {
        String seleccionado = String.valueOf(filtroTipo.getSelectedItem());
        return TODOS.equals(seleccionado)
                || habitacion.getTipo().name().equals(seleccionado);
    }

    private void crear() {
        DatosHabitacion datos = pedirDatos(null);
        habitaciones.crear(
                datos.numero.getText(),
                (TipoHabitacion) datos.tipo.getSelectedItem(),
                VistaUtil.decimal(datos.precio.getText()),
                VistaUtil.entero(datos.camas.getText()),
                "Si".equals(datos.banoPrivado.getSelectedItem()),
                "Si".equals(datos.tv.getSelectedItem())
        );
        refrescar();
    }

    private void modificar() {
        Habitacion actual = habitaciones.buscarPorId(
                VistaUtil.entero(VistaUtil.pedirTexto(this, "Habitacion ID"))
        );
        DatosHabitacion datos = pedirDatos(actual);

        HabitacionBuilder builder = new HabitacionBuilder()
                .conId(actual.getId())
                .paraHotel(actual.getHotelId())
                .conNumero(datos.numero.getText())
                .deTipo((TipoHabitacion) datos.tipo.getSelectedItem())
                .conPrecioPorNoche(VistaUtil.decimal(datos.precio.getText()))
                .conCantidadCamas(VistaUtil.entero(datos.camas.getText()))
                .conEstado(actual.getEstado());

        if ("Si".equals(datos.banoPrivado.getSelectedItem())) {
            builder.conBanoPrivado();
        }
        if ("Si".equals(datos.tv.getSelectedItem())) {
            builder.conTv();
        }

        habitaciones.actualizar(builder.construir());
        refrescar();
    }

    private void eliminar() {
        int id = VistaUtil.entero(VistaUtil.pedirTexto(this, "Habitacion ID"));
        habitaciones.eliminar(id);
        refrescar();
    }

    private DatosHabitacion pedirDatos(Habitacion habitacion) {
        DatosHabitacion datos = new DatosHabitacion(habitacion);

        if (!VistaUtil.confirmarFormulario(
                this,
                habitacion == null ? "Crear habitacion" : "Modificar habitacion",
                "Numero", datos.numero,
                "Tipo", datos.tipo,
                "Precio", datos.precio,
                "Camas", datos.camas,
                "Bano privado", datos.banoPrivado,
                "TV", datos.tv
        )) {
            throw new IllegalArgumentException("Operacion cancelada");
        }

        return datos;
    }

    private static final class DatosHabitacion {

        private final JTextField numero;
        private final JComboBox<TipoHabitacion> tipo;
        private final JTextField precio;
        private final JTextField camas;
        private final JComboBox<String> banoPrivado;
        private final JComboBox<String> tv;

        private DatosHabitacion(Habitacion habitacion) {
            numero = VistaUtil.campo(habitacion == null ? "" : habitacion.getNumero());
            tipo = new JComboBox<>(TipoHabitacion.values());
            precio = VistaUtil.campo(habitacion == null
                    ? "0.00"
                    : habitacion.getPrecioPorNoche().toString());
            camas = VistaUtil.campo(habitacion == null
                    ? "1"
                    : String.valueOf(habitacion.getCantidadCamas()));
            banoPrivado = new JComboBox<>(new String[]{"Si", "No"});
            tv = new JComboBox<>(new String[]{"Si", "No"});

            if (habitacion != null) {
                tipo.setSelectedItem(habitacion.getTipo());
                banoPrivado.setSelectedItem(habitacion.tieneBanoPrivado() ? "Si" : "No");
                tv.setSelectedItem(habitacion.tieneTv() ? "Si" : "No");
            }
        }
    }
}
