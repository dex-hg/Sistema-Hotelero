package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.dao.AutenticacionUsuarioDAO;

import hotel.modelo.entidades.Usuario;
import hotel.modelo.entidades.constantes.RolUsuario;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Objects;
import java.util.Optional;

/**
 * DAO de lectura usado por autenticacion.
 *
 * APLICA PRINCIPIO SOLID: - SRP: ofrece solo la consulta necesaria para validar
 * credenciales. - ISP: implementa una interfaz separada del CRUD de usuarios
 * para que el servicio de autenticacion dependa de una superficie minima.
 */
public final class AutenticacionUsuarioDAOJdbc
        implements AutenticacionUsuarioDAO {

    private static final String COLUMNAS
            = "id, hotel_id, username, password, rol";
    private final EjecutorDAO ejecutorDAO;

    public AutenticacionUsuarioDAOJdbc(ProveedorConexion proveedorConexion) {
        Objects.requireNonNull(proveedorConexion);
        this.ejecutorDAO = new EjecutorDAO(proveedorConexion);
    }

    @Override
    public Optional<Usuario> buscarPorHotelYUsername(
            int hotelId,
            String username
    ) {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM usuarios WHERE hotel_id = ? "
                + "AND username = ?";

        return ejecutorDAO.consultarUno(
                sql,
                this::mapear,
                hotelId,
                username
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
}
