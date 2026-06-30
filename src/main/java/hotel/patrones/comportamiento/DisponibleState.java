package hotel.patrones.comportamiento;

import hotel.modelo.entidades.constantes.EstadoHabitacion;

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
    public EstadoHabitacionState ocupar() {
        return new OcupadaState();
    }

    @Override
    public EstadoHabitacionState enviarAMantenimiento() {
        return new MantenimientoState();
    }
}
