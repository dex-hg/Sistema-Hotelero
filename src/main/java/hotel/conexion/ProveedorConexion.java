package hotel.conexion;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ProveedorConexion {

    Connection obtenerConexion() throws SQLException;

    default void liberarConexion(Connection conexion) throws SQLException {
        conexion.close();
    }
}
