package hotel.patrones.comportamiento;

import hotel.modelo.entidades.constantes.EstadoHabitacion;

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
