package hotel.conexion;

import hotel.excepcion.DAOException;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Objects;

public final class ProveedorConexionTransaccional
        implements ProveedorConexion, EjecutorTransaccional {

    private final ProveedorConexion proveedorBase;
    private final ThreadLocal<Connection> conexionActual = new ThreadLocal<>();

    public ProveedorConexionTransaccional(ProveedorConexion proveedorBase) {
        this.proveedorBase = Objects.requireNonNull(proveedorBase);
    }

    @Override
    public Connection obtenerConexion() throws SQLException {
        Connection conexion = conexionActual.get();
        if (conexion != null) {
            return conexion;
        }

        return proveedorBase.obtenerConexion();
    }

    @Override
    public void liberarConexion(Connection conexion) throws SQLException {
        if (conexion != null && conexion != conexionActual.get()) {
            conexion.close();
        }
    }

    @Override
    public <T> T ejecutar(OperacionTransaccional<T> operacion) {
        Objects.requireNonNull(operacion);

        if (conexionActual.get() != null) {
            return operacion.ejecutar();
        }

        try (Connection conexion = proveedorBase.obtenerConexion()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();

            try {
                conexion.setAutoCommit(false);
                conexionActual.set(conexion);

                T resultado = operacion.ejecutar();
                conexion.commit();
                return resultado;

            } catch (RuntimeException e) {
                conexion.rollback();
                throw e;

            } finally {
                conexionActual.remove();
                conexion.setAutoCommit(autoCommitOriginal);
            }

        } catch (SQLException e) {
            throw new DAOException("No se pudo ejecutar la transaccion", e);
        }
    }
}
