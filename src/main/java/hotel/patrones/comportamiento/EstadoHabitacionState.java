package hotel.patrones.comportamiento;

import hotel.modelo.entidades.constantes.EstadoHabitacion;

public interface EstadoHabitacionState {

    EstadoHabitacion getEstado();

    default EstadoHabitacionState ocupar() {
        throw transicionInvalida("ocupar");
    }

    default EstadoHabitacionState iniciarLimpieza() {
        throw transicionInvalida("iniciar limpieza");
    }

    default EstadoHabitacionState habilitar() {
        throw transicionInvalida("habilitar");
    }

    default EstadoHabitacionState enviarAMantenimiento() {
        throw transicionInvalida("enviar a mantenimiento");
    }

    private TransicionEstadoHabitacionException transicionInvalida(String accion) {
        return new TransicionEstadoHabitacionException(
                "No se puede " + accion + " una habitacion en estado " + getEstado()
        );
    }
}
