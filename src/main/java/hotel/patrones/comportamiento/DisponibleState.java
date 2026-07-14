package hotel.patrones.comportamiento;

import hotel.modelo.entidades.constantes.EstadoHabitacion;

import java.util.Set;

/**
 * Estado disponible de una habitacion.
 *
 * PATRON DE DISENO: - State: define solo las transiciones permitidas desde
 * DISPONIBLE.
 */
public final class DisponibleState implements EstadoHabitacionState {

    @Override
    public EstadoHabitacion getEstado() {
        return EstadoHabitacion.DISPONIBLE;
    }

    @Override
    public Set<EstadoHabitacion> transicionesPermitidas() {
        return Set.of(
                EstadoHabitacion.OCUPADA,
                EstadoHabitacion.MANTENIMIENTO
        );
    }

    @Override
    public EstadoHabitacionState ocupar() {
        return new OcupadaState();
    }

    @Override
    public EstadoHabitacionState enviarAMantenimiento() {
        return new MantenimientoState();
    }
}
