package hotel.modelo.entidades;

import hotel.modelo.entidades.constantes.TipoHabitacion;
import hotel.modelo.entidades.constantes.EstadoHabitacion;

import java.math.BigDecimal;
import java.util.Objects;

public final class Habitacion {

    private final Integer id;
    private final int hotelId;
    private final String numero;
    private final TipoHabitacion tipo;
    private final BigDecimal precioPorNoche;
    private final int cantidadCamas;
    private final boolean banoPrivado;
    private final boolean tv;
    private EstadoHabitacion estado;

    public Habitacion(
            Integer id,
            int hotelId,
            String numero,
            TipoHabitacion tipo,
            BigDecimal precioPorNoche,
            int cantidadCamas,
            boolean banoPrivado,
            boolean tv,
            EstadoHabitacion estado
    ) {

        if (hotelId <= 0) {
            throw new IllegalArgumentException("hotelId debe ser positivo");
        }

        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("numero es obligatorio");
        }

        if (precioPorNoche == null || precioPorNoche.signum() < 0) {
            throw new IllegalArgumentException("precioPorNoche no puede ser negativo");
        }

        if (cantidadCamas <= 0) {
            throw new IllegalArgumentException("cantidadCamas debe ser positiva");
        }

        this.id = id;
        this.hotelId = hotelId;
        this.numero = numero.trim();
        this.tipo = Objects.requireNonNull(tipo, "tipo es obligatorio");
        this.precioPorNoche = precioPorNoche;
        this.cantidadCamas = cantidadCamas;
        this.banoPrivado = banoPrivado;
        this.tv = tv;
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
    }

    public Integer getId() {
        return id;
    }

    public int getHotelId() {
        return hotelId;
    }

    public String getNumero() {
        return numero;
    }

    public TipoHabitacion getTipo() {
        return tipo;
    }

    public BigDecimal getPrecioPorNoche() {
        return precioPorNoche;
    }

    public int getCantidadCamas() {
        return cantidadCamas;
    }

    public boolean tieneBanoPrivado() {
        return banoPrivado;
    }

    public boolean tieneTv() {
        return tv;
    }

    public EstadoHabitacion getEstado() {
        return estado;
    }

}
