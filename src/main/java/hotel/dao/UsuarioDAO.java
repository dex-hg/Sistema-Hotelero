package hotel.dao;

import hotel.modelo.entidades.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioDAO {

    Optional<Usuario> buscarPorId(int id);

    Optional<Usuario> buscarPorUsername(String username);

    List<Usuario> listar();

    Usuario crear(Usuario usuario);

    boolean actualizar(Usuario usuario);

    boolean eliminar(int id);
}
