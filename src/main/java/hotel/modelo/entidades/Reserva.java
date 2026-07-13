package hotel.modelo.entidades;

import hotel.modelo.entidades.constantes.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Reserva {

    private final Integer id;
    private final int hotelId;
    private final int habitacionId;
    private final int clienteId;

    private final LocalDateTime fechaIngreso;
    private final LocalDateTime fechaSalida;
    private final BigDecimal totalHospedaje;
    private BigDecimal montoPagado;
    private EstadoReserva estado;

    public Reserva(
            Integer id,
            int hotelId,
            int habitacionId,
            int clienteId,
            LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida,
            BigDecimal totalHospedaje,
            BigDecimal montoPagado,
            EstadoReserva estado) {

        if (hotelId <= 0 || habitacionId <= 0 || clienteId <= 0) {
            throw new IllegalArgumentException(
                    "Los identificadores deben ser positivos"
            );
        }

        this.fechaIngreso = Objects.requireNonNull(
                fechaIngreso, "fechaIngreso es obligatoria"
        );

        this.fechaSalida = Objects.requireNonNull(
                fechaSalida, "fechaSalida es obligatoria"
        );

        if (!fechaSalida.isAfter(fechaIngreso)) {
            throw new IllegalArgumentException(
                    "fechaSalida debe ser posterior a fechaIngreso"
            );
        }

        if (totalHospedaje == null || totalHospedaje.signum() < 0) {
            throw new IllegalArgumentException(
                    "totalHospedaje no puede ser negativo"
            );
        }
        if (montoPagado == null || montoPagado.signum() < 0) {
            throw new IllegalArgumentException(
                    "montoPagado no puede ser negativo"
            );
        }
        if (montoPagado.compareTo(totalHospedaje) > 0) {
            throw new IllegalArgumentException(
                    "montoPagado no puede superar el total del hospedaje"
            );
        }

        this.id = id;
        this.hotelId = hotelId;
        this.habitacionId = habitacionId;
        this.clienteId = clienteId;
        this.totalHospedaje = totalHospedaje;
        this.montoPagado = montoPagado;
        this.estado = Objects.requireNonNull(
                estado,
                "estado es obligatorio"
        );
    }

    public Integer getId() {
        return id;
    }

    public int getHotelId() {
        return hotelId;
    }

    public int getHabitacionId() {
        return habitacionId;
    }

    public int getClienteId() {
        return clienteId;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public BigDecimal getTotalHospedaje() {
        return totalHospedaje;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void registrarPago(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException(
                    "monto debe ser positivo"
            );
        }

        BigDecimal nuevoMonto = montoPagado.add(monto);
        if (nuevoMonto.compareTo(totalHospedaje) > 0) {
            throw new IllegalArgumentException(
                    "El pago supera el saldo pendiente de la reserva"
            );
        }
        montoPagado = nuevoMonto;
    }

    public void cambiarEstado(EstadoReserva estado) {
        this.estado = Objects.requireNonNull(
                estado,
                "estado es obligatorio"
        );
    }
}
