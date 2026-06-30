package hotel.patrones.comportamiento;

import hotel.modelo.entidades.constantes.EstadoHabitacion;

/**
 * Estado de limpieza de una habitacion.
 *
 * PATRON DE DISENO: - State: permite volver a DISPONIBLE o enviar la habitacion
 * a mantenimiento.
 */
public final class EnLimpiezaState implements EstadoHabitacionState {

    @Override
    public EstadoHabitacion getEstado() {
        return EstadoHabitacion.EN_LIMPIEZA;
    }

    @Override
    public EstadoHabitacionState habilitar() {
        return new DisponibleState();
    }

    @Override
    public EstadoHabitacionState enviarAMantenimiento() {
        return new MantenimientoState();
    }
}
