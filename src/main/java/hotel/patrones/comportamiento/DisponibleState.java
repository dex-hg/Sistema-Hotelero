package hotel.patrones.comportamiento;

import hotel.modelo.entidades.constantes.EstadoHabitacion;

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
