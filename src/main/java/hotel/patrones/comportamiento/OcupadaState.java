package hotel.patrones.comportamiento;

import hotel.modelo.entidades.constantes.EstadoHabitacion;

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
