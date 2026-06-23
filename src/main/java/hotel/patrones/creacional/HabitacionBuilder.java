package hotel.patrones.creacional;

import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.TipoHabitacion;

import java.math.BigDecimal;

public final class HabitacionBuilder {

    private Integer id;
    private int hotelId;
    private String numero;
    private TipoHabitacion tipo;
    private BigDecimal precioPorNoche;
    private int cantidadCamas;
    private boolean banoPrivado;
    private boolean tv;
    private EstadoHabitacion estado = EstadoHabitacion.DISPONIBLE;

    public HabitacionBuilder conId(Integer id) {
        this.id = id;
        return this;
    }

    public HabitacionBuilder paraHotel(int hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    public HabitacionBuilder conNumero(String numero) {
        this.numero = numero;
        return this;
    }

    public HabitacionBuilder deTipo(TipoHabitacion tipo) {
        this.tipo = tipo;
        return this;
    }

    public HabitacionBuilder conPrecioPorNoche(BigDecimal precioPorNoche) {
        this.precioPorNoche = precioPorNoche;
        return this;
    }

    public HabitacionBuilder conCantidadCamas(int cantidadCamas) {
        this.cantidadCamas = cantidadCamas;
        return this;
    }

    public HabitacionBuilder conBanoPrivado() {
        this.banoPrivado = true;
        return this;
    }

    public HabitacionBuilder conTv() {
        this.tv = true;
        return this;
    }

    public HabitacionBuilder conEstado(EstadoHabitacion estado) {
        this.estado = estado;
        return this;
    }

    public Habitacion construir() {
        return new Habitacion(
                id,
                hotelId,
                numero,
                tipo,
                precioPorNoche,
                cantidadCamas,
                banoPrivado,
                tv,
                estado
        );
    }
}
