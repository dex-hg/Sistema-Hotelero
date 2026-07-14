package hotel.modelo.servicio;

import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.constantes.TipoHabitacion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HabitacionServicio {

    Habitacion buscarPorId(int id);

    Optional<Habitacion> buscarPorNumero(String numero);

    List<Habitacion> listar();

    Habitacion crear(
            String numero,
            TipoHabitacion tipo,
            BigDecimal precioPorNoche,
            int cantidadCamas,
            boolean banoPrivado,
            boolean tv
    );

    Habitacion actualizar(
            int id,
            String numero,
            TipoHabitacion tipo,
            BigDecimal precioPorNoche,
            int cantidadCamas,
            boolean banoPrivado,
            boolean tv
    );

    boolean eliminar(int id);

    Habitacion ocupar(int id);

    Habitacion iniciarLimpieza(int id);

    Habitacion habilitar(int id);

    Habitacion enviarAMantenimiento(int id);
}
