package hotel.vista.panel;

import hotel.controlador.EstadisticasControlador;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.modelo.servicio.ResumenReportes;
import hotel.vista.VistaUtil;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import java.util.Objects;

public final class PanelReportes extends JPanel implements PanelActualizable {

    private final EstadisticasControlador estadisticas;

    private final JPanel tarjetas = new JPanel(
            new GridLayout(2, 3, 18, 18)
    );
    private final DefaultTableModel modeloEstados = VistaUtil.modeloTabla(
            new Object[]{"Estado", "Cantidad"},
            new Class<?>[]{EstadoReserva.class, Integer.class}
    );

    public PanelReportes(
            EstadisticasControlador estadisticas
    ) {
        super(new BorderLayout(18, 18));
        this.estadisticas = Objects.requireNonNull(estadisticas);
        construir();
        refrescarAsync();
    }

    private void construir() {
        JButton actualizar = VistaUtil.botonCompacto("Actualizar datos");
        actualizar.addActionListener(e -> refrescarAsync());

        setBorder(
                javax.swing.BorderFactory.createEmptyBorder(
                        18,
                        18,
                        18,
                        18
                )
        );
        add(VistaUtil.encabezadoModulo(
                "Reportes administrativos",
                "Indicadores útiles para el dueño del negocio",
                actualizar
        ), BorderLayout.NORTH);

        JTable tablaEstados = new JTable(modeloEstados);
        VistaUtil.configurarTabla(tablaEstados, modeloEstados);

        JPanel contenido = new JPanel(new BorderLayout(18, 18));
        contenido.add(tarjetas, BorderLayout.NORTH);
        contenido.add(
                VistaUtil.seccion(
                        "Reservas por estado",
                        new JScrollPane(tablaEstados)
                ),
                BorderLayout.CENTER
        );
        add(contenido, BorderLayout.CENTER);
    }

    @Override
    public void refrescarAsync() {
        VistaUtil.ejecutarAsync(
                this,
                null,
                estadisticas::obtenerReportesActuales,
                this::aplicarDatos,
                null
        );
    }

    private void aplicarDatos(ResumenReportes datos) {
        tarjetas.removeAll();
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Habitación más reservada",
                datos.habitacionMasReservada(),
                "HAB",
                new Color(25, 118, 210)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Días promedio de estadía",
                String.format("%.1f", datos.diasPromedioEstadia()),
                "DÍAS",
                new Color(20, 135, 84)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Valor de reservas del mes",
                datos.valorReservasMes().toPlainString(),
                "S/",
                new Color(230, 126, 34)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Huéspedes registrados",
                String.valueOf(datos.cantidadClientes()),
                "HUE",
                new Color(111, 66, 193)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Huéspedes recurrentes",
                String.valueOf(datos.huespedesRecurrentes()),
                "REC",
                new Color(13, 110, 253)
        ));
        tarjetas.add(VistaUtil.tarjetaResumen(
                "Reservas activas",
                String.valueOf(datos.reservasPorEstado().getOrDefault(
                        EstadoReserva.ACTIVA,
                        0
                )),
                "ACT",
                new Color(190, 45, 55)
        ));

        cargarEstados(datos);
        revalidate();
        repaint();
    }

    private void cargarEstados(ResumenReportes datos) {
        modeloEstados.setRowCount(0);
        for (EstadoReserva estado : EstadoReserva.values()) {
            modeloEstados.addRow(new Object[]{
                estado,
                datos.reservasPorEstado().getOrDefault(estado, 0)
            });
        }
    }
}
