package hotel.vista;

import hotel.controlador.ReservaControlador;

import hotel.modelo.entidades.Reserva;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;

import java.util.Objects;

public final class PanelReservas extends JPanel {

    private final ReservaControlador reservas;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{
                "ID", "Habitacion", "Cliente", "Ingreso",
                "Salida", "Pagado", "Estado"
            },
            0
    );

    public PanelReservas(ReservaControlador reservas) {
        super(new BorderLayout(10, 10));
        this.reservas = Objects.requireNonNull(reservas);
        construir();
        refrescar();
    }

    private void construir() {
        JTable tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);

        JButton refrescar = new JButton("Refrescar");
        refrescar.addActionListener(
                e -> VistaUtil.ejecutar(
                        this,
                        this::refrescar
                )
        );

        JButton crear = new JButton("Crear");
        crear.addActionListener(
                e -> VistaUtil.ejecutar(
                        this,
                        this::crear
                )
        );

        JButton pago = new JButton("Registrar pago");
        pago.addActionListener(
                e -> VistaUtil.ejecutar(
                        this,
                        this::registrarPago
                )
        );

        JButton checkIn = new JButton("Check-in");
        checkIn.addActionListener(
                e -> VistaUtil.ejecutar(
                        this,
                        this::registrarCheckIn
                )
        );

        JButton checkOut = new JButton("Check-out");
        checkOut.addActionListener(
                e -> VistaUtil.ejecutar(
                        this,
                        this::registrarCheckOut
                )
        );

        JButton cancelar = new JButton("Cancelar");
        cancelar.addActionListener(
                e -> VistaUtil.ejecutar(
                        this,
                        this::cancelar
                )
        );

        JButton finalizar = new JButton("Finalizar");
        finalizar.addActionListener(
                e -> VistaUtil.ejecutar(
                        this,
                        this::finalizar
                )
        );

        JButton eliminar = new JButton("Eliminar");
        eliminar.addActionListener(
                e -> VistaUtil.ejecutar(
                        this,
                        this::eliminar
                )
        );

        JPanel acciones = new JPanel();
        acciones.add(refrescar);
        acciones.add(crear);
        acciones.add(pago);
        acciones.add(checkIn);
        acciones.add(checkOut);
        acciones.add(cancelar);
        acciones.add(finalizar);
        acciones.add(eliminar);

        setBorder(
                javax.swing.BorderFactory.createEmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );
        add(acciones, BorderLayout.NORTH);
        add(
                new JScrollPane(tabla),
                BorderLayout.CENTER
        );
    }

    private void refrescar() {
        modelo.setRowCount(0);
        for (Reserva reserva : reservas.listar()) {
            modelo.addRow(new Object[]{
                reserva.getId(),
                reserva.getHabitacionId(),
                reserva.getClienteId(),
                VistaUtil.FORMATO_FECHA.format(
                reserva.getFechaIngreso()
                ),
                VistaUtil.FORMATO_FECHA.format(
                reserva.getFechaSalida()
                ),
                reserva.getTotalPagado(),
                reserva.getEstado()
            });
        }
    }

    private void crear() {
        JTextField habitacionId = VistaUtil.campo("");
        JTextField clienteId = VistaUtil.campo("");
        JTextField ingreso = VistaUtil.campo("2026-07-01 14:00");
        JTextField salida = VistaUtil.campo("2026-07-02 12:00");
        JTextField pagado = VistaUtil.campo("0.00");

        if (!VistaUtil.confirmarFormulario(
                this,
                "Crear reserva",
                "Habitacion ID", habitacionId,
                "Cliente ID", clienteId,
                "Ingreso", ingreso,
                "Salida", salida,
                "Total pagado", pagado
        )) {
            return;
        }

        reservas.crear(
                VistaUtil.entero(habitacionId.getText()),
                VistaUtil.entero(clienteId.getText()),
                VistaUtil.fecha(ingreso.getText()),
                VistaUtil.fecha(salida.getText()),
                VistaUtil.decimal(pagado.getText())
        );
        refrescar();
    }

    private void registrarPago() {
        reservas.registrarPago(
                pedirReservaId(),
                VistaUtil.decimal(
                        VistaUtil.pedirTexto(
                                this,
                                "Monto"
                        )
                )
        );
        refrescar();
    }

    private void registrarCheckIn() {
        reservas.registrarCheckIn(pedirReservaId());
        refrescar();
    }

    private void registrarCheckOut() {
        reservas.registrarCheckOut(pedirReservaId());
        refrescar();
    }

    private void cancelar() {
        reservas.cancelar(pedirReservaId());
        refrescar();
    }

    private void finalizar() {
        reservas.finalizar(pedirReservaId());
        refrescar();
    }

    private void eliminar() {
        reservas.eliminar(pedirReservaId());
        refrescar();
    }

    private int pedirReservaId() {
        return VistaUtil.entero(
                VistaUtil.pedirTexto(
                        this,
                        "Reserva ID"
                )
        );
    }
}
