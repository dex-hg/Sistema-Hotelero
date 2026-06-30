package hotel.patrones.comportamiento;

import hotel.modelo.entidades.constantes.EstadoHabitacion;

/**
 * Estado de mantenimiento de una habitacion.
 *
 * PATRON DE DISENO: - State: obliga a pasar por limpieza antes de volver a
 * estar disponible.
 */
public final class MantenimientoState implements EstadoHabitacionState {

    @Override
    public EstadoHabitacion getEstado() {
        return EstadoHabitacion.MANTENIMIENTO;
    }

    @Override
    public EstadoHabitacionState iniciarLimpieza() {
        return new EnLimpiezaState();
    }
}
