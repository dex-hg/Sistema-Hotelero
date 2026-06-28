package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;
import hotel.dao.AutenticacionUsuarioDAO;
import hotel.excepcion.DAOException;
import hotel.modelo.entidades.Usuario;
import hotel.modelo.entidades.constantes.RolUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Objects;
import java.util.Optional;

public final class AutenticacionUsuarioDAOJdbc implements AutenticacionUsuarioDAO {

    private static final String COLUMNAS = "id, hotel_id, username, password, rol";

    private final ProveedorConexion proveedorConexion;

    public AutenticacionUsuarioDAOJdbc(ProveedorConexion proveedorConexion) {
        this.proveedorConexion = Objects.requireNonNull(proveedorConexion);
    }

    @Override
    public Optional<Usuario> buscarPorHotelYUsername(int hotelId, String username) {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM usuarios "
                + "WHERE hotel_id = ? "
                + "AND username = ?";

        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, hotelId);
            sentencia.setString(2, username);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next()
                        ? Optional.of(mapear(resultado))
                        : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el usuario para autenticacion", e);
        }
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
}
