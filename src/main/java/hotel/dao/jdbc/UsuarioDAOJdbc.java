package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.dao.UsuarioDAO;

import hotel.modelo.entidades.Usuario;
import hotel.modelo.entidades.constantes.RolUsuario;
import hotel.modelo.sesion.ProveedorHotelId;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementacion JDBC de {@link UsuarioDAO} para usuarios del hotel activo.
 *
 * APLICA PRINCIPIO SOLID: - SRP: solo persiste usuarios. - ISP: usa
 * {@link ProveedorHotelId} para obtener el tenant sin depender de operaciones
 * completas de sesion.
 */
public final class UsuarioDAOJdbc implements UsuarioDAO {

    private static final String COLUMNAS
            = "id, hotel_id, username, password, rol";
    private final ProveedorHotelId proveedorHotelId;
    private final EjecutorDAO ejecutorDAO;

    public UsuarioDAOJdbc(
            ProveedorConexion proveedorConexion,
            ProveedorHotelId proveedorHotelId
    ) {
        Objects.requireNonNull(proveedorConexion);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
        this.ejecutorDAO = new EjecutorDAO(proveedorConexion);
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM usuarios WHERE hotel_id = ? AND id = ?";

        return ejecutorDAO.consultarUno(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId(),
                id
        );
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM usuarios WHERE hotel_id = ? AND username = ?";

        return ejecutorDAO.consultarUno(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId(),
                username
        );
    }

    @Override
    public List<Usuario> listar() {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM usuarios WHERE hotel_id = ? ORDER BY username";

        return ejecutorDAO.consultarLista(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId()
        );
    }

    @Override
    public Usuario crear(Usuario usuario) {
        String sql
                = "INSERT INTO usuarios "
                + "(hotel_id, username, password, rol) "
                + "VALUES (?, ?, ?, ?)";
        int id = ejecutorDAO.crearYObtenerId(sql,
                proveedorHotelId.getHotelId(),
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getRol().name()
        );

        return new Usuario(
                id,
                proveedorHotelId.getHotelId(),
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getRol()
        );
    }

    @Override
    public boolean actualizar(Usuario usuario) {
        exigirId(usuario.getId());
        String sql
                = "UPDATE usuarios "
                + "SET username = ?, "
                + "password = ?, "
                + "rol = ? "
                + "WHERE hotel_id = ? "
                + "AND id = ?";

        return ejecutorDAO.ejecutarModificacion(sql,
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getRol().name(),
                proveedorHotelId.getHotelId(),
                usuario.getId()
        );
    }

    @Override
    public boolean eliminar(int id) {
        String sql
                = "DELETE FROM usuarios "
                + "WHERE hotel_id = ? "
                + "AND id = ?";

        return ejecutorDAO.ejecutarModificacion(
                sql,
                proveedorHotelId.getHotelId(),
                id
        );
    }

    private Usuario mapear(ResultSet resultado) throws SQLException {
        return new Usuario(
                resultado.getInt("id"),
                resultado.getInt("hotel_id"),
                resultado.getString("username"),
                resultado.getString("password"),
                RolUsuario.valueOf(resultado.getString("rol"))
        );
    }

    private void exigirId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El usuario debe tener id para actualizarse"
            );
        }
    }
}
