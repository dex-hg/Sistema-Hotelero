package hotel.patrones.comportamiento;

import hotel.modelo.entidades.constantes.EstadoHabitacion;

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
