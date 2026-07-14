package hotel.dao;

import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.Cliente;

import java.util.List;
import java.util.Optional;
import java.util.Map;

public interface ReservaDAO {

    Optional<Reserva> buscarPorId(int id);

    List<Reserva> listar();

    Reserva crear(Reserva reserva);

    void asociarHuesped(int reservaId, int clienteId, boolean principal);

    List<Cliente> listarHuespedes(int reservaId);

    Map<Integer, List<Cliente>> listarHuespedesPorReserva();

    boolean existeReservaActivaParaCliente(int clienteId);

    boolean actualizar(Reserva reserva);

    boolean eliminar(int id);
}
