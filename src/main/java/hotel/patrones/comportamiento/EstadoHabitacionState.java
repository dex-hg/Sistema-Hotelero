package hotel.patrones.comportamiento;

import hotel.excepcion.TransicionEstadoHabitacionException;
import hotel.modelo.entidades.constantes.EstadoHabitacion;

import java.util.Set;

/**
 * Interfaz que define las transiciones y comportamiento de cada estado de una
 * habitación (Disponible, Ocupada, Limpieza, Mantenimiento).
 *
 * PATRÓN DE DISEÑO: - State: Permite a una habitación cambiar su comportamiento
 * dinámicamente cuando su estado interno varía. Al delegar las transiciones a
 * subclases concretas como {@code DisponibleState} u {@code OcupadaState}, se
 * elimina la necesidad de sentencias condicionales complejas dentro de la
 * entidad {@code Habitacion}.
 *
 * APLICA PRINCIPIO SOLID: - OCP (Open/Closed Principle): Las reglas propias de
 * cada estado se extienden en implementaciones separadas. Agregar un valor al
 * enum todavía exige actualizar la fábrica que reconstruye el estado, pero no
 * obliga a introducir condicionales de transición dentro de la entidad.
 */
public interface EstadoHabitacionState {

    EstadoHabitacion getEstado();

    default Set<EstadoHabitacion> transicionesPermitidas() {
        return Set.of();
    }

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

    private TransicionEstadoHabitacionException transicionInvalida(
            String accion
    ) {
        return new TransicionEstadoHabitacionException(
                "No se puede "
                + accion
                + " una habitacion en estado "
                + getEstado()
        );
    }
}
