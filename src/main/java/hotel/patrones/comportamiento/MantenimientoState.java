package hotel.patrones.comportamiento;

import hotel.modelo.entidades.constantes.EstadoHabitacion;

import java.util.Set;

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
    public Set<EstadoHabitacion> transicionesPermitidas() {
        return Set.of(EstadoHabitacion.EN_LIMPIEZA);
    }

    @Override
    public EstadoHabitacionState iniciarLimpieza() {
        return new EnLimpiezaState();
    }
}
