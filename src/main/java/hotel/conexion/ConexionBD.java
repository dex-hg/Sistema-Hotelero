package hotel.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionBD implements ProveedorConexion {

    private final String url;
    private final String usuario;
    private final String password;

    public ConexionBD() {
        this(
                ConexionConfig.URL,
                ConexionConfig.USER,
                ConexionConfig.PASSWORD
        );
    }

    public ConexionBD(String url, String usuario, String password) {
        this.url = url;
        this.usuario = usuario;
        this.password = password;
    }

    @Override
    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }
}
