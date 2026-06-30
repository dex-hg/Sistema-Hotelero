package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.excepcion.DAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Centraliza la ejecución JDBC repetida de los DAO concretos.
 *
 * APLICA PRINCIPIO SOLID: - SRP: esta clase se dedica solo a preparar
 * sentencias, asignar parámetros, mapear resultados y liberar conexiones; cada
 * DAO conserva la responsabilidad de definir su SQL y sus reglas de filtrado
 * por tenant.
 */
public final class EjecutorDAO {

    private final ProveedorConexion proveedorConexion;

    public EjecutorDAO(ProveedorConexion proveedorConexion) {
        this.proveedorConexion = Objects.requireNonNull(
                proveedorConexion
        );
    }

    /**
     * Ejecuta una consulta SQL que retorna como máximo una sola fila y la mapea
     * a un Optional.
     */
    public <T> Optional<T> consultarUno(
            String sql,
            Mapeador<T> mapeador,
            Object... parametros
    ) {
        Connection conexion = null;

        try {
            conexion = proveedorConexion.obtenerConexion();
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                configurarParametros(sentencia, parametros);

                try (ResultSet rs = sentencia.executeQuery()) {
                    return rs.next() ? Optional.of(
                            mapeador.mapear(rs)
                    ) : Optional.empty();
                }
            }

        } catch (SQLException e) {
            throw new DAOException(
                    "Error al ejecutar consulta de fila unica",
                    e
            );

        } finally {
            liberarConexion(conexion);
        }
    }

    /**
     * Ejecuta una consulta SQL que retorna múltiples filas y las mapea a una
     * lista.
     */
    public <T> List<T> consultarLista(
            String sql,
            Mapeador<T> mapeador,
            Object... parametros
    ) {
        Connection conexion = null;
        try {
            conexion = proveedorConexion.obtenerConexion();
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                configurarParametros(sentencia, parametros);

                try (ResultSet rs = sentencia.executeQuery()) {
                    List<T> resultado = new ArrayList<>();

                    while (rs.next()) {
                        resultado.add(mapeador.mapear(rs));
                    }

                    return resultado;
                }
            }

        } catch (SQLException e) {
            throw new DAOException(
                    "Error al ejecutar consulta de lista",
                    e
            );

        } finally {
            liberarConexion(conexion);
        }
    }

    /**
     * Ejecuta un UPDATE o DELETE esperado para una única fila lógica.
     *
     * El contrato de los DAO por ID sigue siendo exacto: retorna true solo si
     * la base de datos afectó una fila. Esto evita esconder errores de SQL que
     * puedan impactar varias filas por accidente.
     */
    public boolean ejecutarModificacion(String sql, Object... parametros) {
        Connection conexion = null;
        try {
            conexion = proveedorConexion.obtenerConexion();

            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                configurarParametros(sentencia, parametros);

                return sentencia.executeUpdate() == 1;
            }

        } catch (SQLException e) {
            throw new DAOException(
                    "Error al ejecutar modificacion de base de datos",
                    e
            );

        } finally {
            liberarConexion(conexion);
        }
    }

    /**
     * Ejecuta una instrucción SQL de inserción y retorna el ID autogenerado por
     * la base de datos.
     */
    public int crearYObtenerId(String sql, Object... parametros) {
        Connection conexion = null;
        try {
            conexion = proveedorConexion.obtenerConexion();
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                configurarParametros(sentencia, parametros);
                sentencia.executeUpdate();

                try (ResultSet claves = sentencia.getGeneratedKeys()) {
                    if (!claves.next()) {
                        throw new DAOException(
                                "La base de datos "
                                + "no retorno el ID generado"
                        );
                    }
                    return claves.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new DAOException(
                    "Error al ejecutar insercion de base de datos",
                    e
            );

        } finally {
            liberarConexion(conexion);
        }
    }

    private void configurarParametros(
            PreparedStatement sentencia,
            Object[] parametros)
            throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            Object param = parametros[i];

            if (param instanceof java.time.LocalDateTime localDateTime) {
                sentencia.setTimestamp(
                        i + 1,
                        java.sql.Timestamp.valueOf(localDateTime)
                );
            } else {
                sentencia.setObject(i + 1, param);
            }
        }
    }

    private void liberarConexion(Connection conexion) {
        if (conexion == null) {
            return;
        }
        try {
            proveedorConexion.liberarConexion(conexion);
        } catch (SQLException e) {
            throw new DAOException(
                    "Error al liberar la conexion a la base de datos",
                    e
            );
        }
    }

    @FunctionalInterface
    public interface Mapeador<T> {

        T mapear(ResultSet rs) throws SQLException;
    }
}
