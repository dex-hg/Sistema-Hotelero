package hotel.dao;

import hotel.modelo.entidades.Reserva;

import java.util.List;
import java.util.Optional;

public interface ReservaDAO {

    Optional<Reserva> buscarPorId(int id);

    List<Reserva> listar();

    Reserva crear(Reserva reserva);

    boolean actualizar(Reserva reserva);

    boolean eliminar(int id);
}
