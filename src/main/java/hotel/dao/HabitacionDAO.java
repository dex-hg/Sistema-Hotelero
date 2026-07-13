package hotel.dao;

import hotel.modelo.entidades.Habitacion;

import java.util.List;
import java.util.Optional;

public interface HabitacionDAO {

    Optional<Habitacion> buscarPorId(int id);

    /**
     * Busca y bloquea la fila hasta terminar la transacción actual. Se usa al
     * comprobar disponibilidad para evitar reservas concurrentes solapadas.
     */
    Optional<Habitacion> buscarPorIdParaActualizar(int id);

    Optional<Habitacion> buscarPorNumero(String numero);

    List<Habitacion> listar();

    Habitacion crear(Habitacion habitacion);

    boolean actualizar(Habitacion habitacion);

    boolean eliminar(int id);
}
