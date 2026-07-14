package hotel.dao;

import hotel.modelo.entidades.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteDAO {

    Optional<Cliente> buscarPorId(int id);

    Optional<Cliente> buscarPorIdParaActualizar(int id);

    Optional<Cliente> buscarPorDocumento(String documentoIdentidad);

    List<Cliente> listar();

    Cliente crear(Cliente cliente);

    boolean actualizar(Cliente cliente);

    boolean eliminar(int id);
}
