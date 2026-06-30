package hotel.patrones.comportamiento;

import hotel.excepcion.TransicionEstadoHabitacionException;
import hotel.modelo.entidades.constantes.EstadoHabitacion;

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
 * APLICA PRINCIPIO SOLID: - OCP (Open/Closed Principle): El sistema está
 * abierto a la extensión (podemos agregar un nuevo estado como "Bloqueado" o
 * "Fuera de Servicio" implementando esta interfaz) pero cerrado a la
 * modificación (no necesitamos modificar las clases de los estados existentes
 * ni alterar la entidad principal {@code Habitacion}).
 */
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
