package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;
import hotel.dao.ClienteDAO;
import hotel.excepcion.DAOException;
import hotel.modelo.entidades.Cliente;
import hotel.modelo.sesion.ProveedorHotelId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ClienteDAOJdbc implements ClienteDAO {

    private static final String COLUMNAS
            = "id,"
            + " hotel_id,"
            + " nombre_completo,"
            + " documento_identidad,"
            + " telefono";

    private final ProveedorConexion proveedorConexion;
    private final ProveedorHotelId proveedorHotelId;

    public ClienteDAOJdbc(
            ProveedorConexion proveedorConexion,
            ProveedorHotelId proveedorHotelId
    ) {
        this.proveedorConexion = Objects.requireNonNull(proveedorConexion);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        return buscarUno(
                "SELECT "
                + COLUMNAS
                + " FROM clientes WHERE hotel_id = ? AND id = ?",
                id
        );
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM clientes WHERE hotel_id = ? "
                + "AND documento_identidad = ?";

        Connection conexion = null;
        try {
            conexion = proveedorConexion.obtenerConexion();
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setInt(1, proveedorHotelId.getHotelId());
                sentencia.setString(2, documento);

                try (ResultSet resultado = sentencia.executeQuery()) {
                    return resultado.next()
                            ? Optional.of(mapear(resultado))
                            : Optional.empty();
                }
            }

        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el cliente", e);
        } finally {
            liberarConexion(conexion);
        }
    }

    @Override
    public List<Cliente> listar() {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM clientes WHERE hotel_id = ? "
                + "ORDER BY nombre_completo";
        List<Cliente> clientes = new ArrayList<>();

        Connection conexion = null;
        try {
            conexion = proveedorConexion.obtenerConexion();
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setInt(1, proveedorHotelId.getHotelId());

                try (ResultSet resultado = sentencia.executeQuery()) {
                    while (resultado.next()) {
                        clientes.add(mapear(resultado));
                    }
                }

                return clientes;
            }

        } catch (SQLException e) {
            throw new DAOException("No se pudieron listar los clientes", e);
        } finally {
            liberarConexion(conexion);
        }
    }

    @Override
    public Cliente crear(Cliente cliente) {
        String sql
                = "INSERT INTO "
                + "clientes (hotel_id, nombre_completo, documento_identidad, telefono) "
                + "VALUES (?, ?, ?, ?)";

        Connection conexion = null;
        try {
            conexion = proveedorConexion.obtenerConexion();
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            )) {
                sentencia.setInt(1, proveedorHotelId.getHotelId());
                sentencia.setString(2, cliente.getNombreCompleto());
                sentencia.setString(3, cliente.getDocumentoIdentidad());
                sentencia.setString(4, cliente.getTelefono());
                sentencia.executeUpdate();

                try (ResultSet claves = sentencia.getGeneratedKeys()) {
                    if (!claves.next()) {
                        throw new DAOException("PostgreSQL no devolvio el id del cliente");
                    }

                    return new Cliente(
                            claves.getInt(1),
                            proveedorHotelId.getHotelId(),
                            cliente.getNombreCompleto(),
                            cliente.getDocumentoIdentidad(),
                            cliente.getTelefono()
                    );
                }
            }

        } catch (SQLException e) {
            throw new DAOException("No se pudo crear el cliente", e);
        } finally {
            liberarConexion(conexion);
        }
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        exigirId(cliente.getId());
        String sql
                = "UPDATE clientes "
                + "SET nombre_completo = ?, "
                + "documento_identidad = ?, "
                + "telefono = ? "
                + "WHERE hotel_id = ? "
                + "AND id = ?";

        Connection conexion = null;
        try {
            conexion = proveedorConexion.obtenerConexion();
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setString(1, cliente.getNombreCompleto());
                sentencia.setString(2, cliente.getDocumentoIdentidad());
                sentencia.setString(3, cliente.getTelefono());
                sentencia.setInt(4, proveedorHotelId.getHotelId());
                sentencia.setInt(5, cliente.getId());

                return sentencia.executeUpdate() == 1;
            }

        } catch (SQLException e) {
            throw new DAOException("No se pudo actualizar el cliente", e);
        } finally {
            liberarConexion(conexion);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql
                = "DELETE FROM clientes "
                + "WHERE hotel_id = ? "
                + "AND id = ?";

        Connection conexion = null;
        try {
            conexion = proveedorConexion.obtenerConexion();
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setInt(1, proveedorHotelId.getHotelId());
                sentencia.setInt(2, id);

                return sentencia.executeUpdate() == 1;
            }

        } catch (SQLException e) {
            throw new DAOException("No se pudo eliminar el cliente", e);
        } finally {
            liberarConexion(conexion);
        }
    }

    private Optional<Cliente> buscarUno(String sql, int id) {
        Connection conexion = null;
        try {
            conexion = proveedorConexion.obtenerConexion();
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setInt(1, proveedorHotelId.getHotelId());
                sentencia.setInt(2, id);

                try (ResultSet resultado = sentencia.executeQuery()) {
                    return resultado.next()
                            ? Optional.of(mapear(resultado))
                            : Optional.empty();
                }
            }

        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el cliente", e);
        } finally {
            liberarConexion(conexion);
        }
    }

    private Cliente mapear(ResultSet resultado) throws SQLException {
        return new Cliente(
                resultado.getInt("id"),
                resultado.getInt("hotel_id"),
                resultado.getString("nombre_completo"),
                resultado.getString("documento_identidad"),
                resultado.getString("telefono")
        );
    }

    private void exigirId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El cliente debe tener id para actualizarse");
        }
    }

    private void liberarConexion(Connection conexion) {
        if (conexion == null) {
            return;
        }

        try {
            proveedorConexion.liberarConexion(conexion);
        } catch (SQLException e) {
            throw new DAOException("No se pudo liberar la conexion de clientes", e);
        }
    }
}
