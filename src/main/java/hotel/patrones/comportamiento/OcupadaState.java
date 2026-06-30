package hotel.patrones.comportamiento;

import hotel.modelo.entidades.constantes.EstadoHabitacion;

/**
 * Estado ocupado de una habitacion.
 *
 * PATRON DE DISENO: - State: desde OCUPADA solo se permite iniciar limpieza al
 * terminar la estadia.
 */
public final class OcupadaState implements EstadoHabitacionState {

    @Override
    public EstadoHabitacion getEstado() {
        return EstadoHabitacion.OCUPADA;
    }

    @Override
    public EstadoHabitacionState iniciarLimpieza() {
        return new EnLimpiezaState();
    }
}
