package hotel.controlador;

import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.constantes.TipoHabitacion;
import hotel.modelo.servicio.HabitacionServicio;

import java.math.BigDecimal;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class HabitacionControlador {

    private final HabitacionServicio servicio;

    public HabitacionControlador(HabitacionServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio);
    }

    public List<Habitacion> listar() {
        return servicio.listar();
    }

    public Habitacion buscarPorId(int id) {
        return servicio.buscarPorId(id);
    }

    public Optional<Habitacion> buscarPorNumero(String numero) {
        return servicio.buscarPorNumero(numero);
    }

    public Habitacion crear(
            String numero,
            TipoHabitacion tipo,
            BigDecimal precio,
            int camas,
            boolean banoPrivado,
            boolean tv
    ) {
        return servicio.crear(
                numero, tipo,
                precio,
                camas,
                banoPrivado, tv
        );
    }

    public Habitacion actualizar(
            int id,
            String numero,
            TipoHabitacion tipo,
            BigDecimal precio,
            int camas,
            boolean banoPrivado,
            boolean tv
    ) {
        return servicio.actualizar(
                id,
                numero,
                tipo,
                precio,
                camas,
                banoPrivado,
                tv
        );
    }

    public boolean eliminar(int id) {
        return servicio.eliminar(id);
    }

    public Habitacion ocupar(int id) {
        return servicio.ocupar(id);
    }

    public Habitacion iniciarLimpieza(int id) {
        return servicio.iniciarLimpieza(id);
    }

    public Habitacion habilitar(int id) {
        return servicio.habilitar(id);
    }

    public Habitacion enviarAMantenimiento(int id) {
        return servicio.enviarAMantenimiento(id);
    }
}
