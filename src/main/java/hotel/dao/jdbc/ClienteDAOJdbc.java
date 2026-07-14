package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.dao.ClienteDAO;

import hotel.modelo.entidades.Cliente;
import hotel.modelo.sesion.ProveedorHotelId;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementacion JDBC de {@link ClienteDAO} limitada siempre al hotel activo.
 *
 * APLICA PRINCIPIO SOLID: - SRP: solo traduce operaciones de clientes a SQL. -
 * ISP: depende de {@link ProveedorHotelId}, una interfaz minima para conocer el
 * tenant, en vez de acoplarse al contexto completo de sesion.
 */
public final class ClienteDAOJdbc implements ClienteDAO {

    private static final String COLUMNAS
            = "id,"
            + " hotel_id,"
            + " nombre_completo,"
            + " documento_identidad,"
            + " telefono";

    private final ProveedorHotelId proveedorHotelId;
    private final EjecutorDAO ejecutorDAO;

    public ClienteDAOJdbc(
            ProveedorConexion proveedorConexion,
            ProveedorHotelId proveedorHotelId
    ) {
        Objects.requireNonNull(proveedorConexion);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
        this.ejecutorDAO = new EjecutorDAO(proveedorConexion);
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM clientes WHERE hotel_id = ? "
                + "AND id = ?";

        return ejecutorDAO.consultarUno(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId(),
                id
        );
    }

    @Override
    public Optional<Cliente> buscarPorIdParaActualizar(int id) {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM clientes WHERE hotel_id = ? "
                + "AND id = ? FOR UPDATE";

        return ejecutorDAO.consultarUno(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId(),
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

        return ejecutorDAO.consultarUno(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId(),
                documento
        );
    }

    @Override
    public List<Cliente> listar() {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM clientes WHERE hotel_id = ? "
                + "ORDER BY nombre_completo";

        return ejecutorDAO.consultarLista(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId()
        );
    }

    @Override
    public Cliente crear(Cliente cliente) {
        String sql
                = "INSERT INTO clientes "
                + "(hotel_id, nombre_completo, documento_identidad, telefono) "
                + "VALUES (?, ?, ?, ?)";

        int id = ejecutorDAO.crearYObtenerId(
                sql,
                proveedorHotelId.getHotelId(),
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getTelefono()
        );

        return new Cliente(
                id,
                proveedorHotelId.getHotelId(),
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getTelefono()
        );
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        exigirId(cliente.getId());
        String sql
                = "UPDATE clientes SET "
                + "nombre_completo = ?, "
                + "documento_identidad = ?, "
                + "telefono = ? "
                + "WHERE hotel_id = ? "
                + "AND id = ?";

        return ejecutorDAO.ejecutarModificacion(
                sql,
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getTelefono(),
                proveedorHotelId.getHotelId(),
                cliente.getId()
        );
    }

    @Override
    public boolean eliminar(int id) {
        String sql
                = "DELETE FROM clientes "
                + "WHERE hotel_id = ? "
                + "AND id = ?";

        return ejecutorDAO.ejecutarModificacion(
                sql,
                proveedorHotelId.getHotelId(),
                id
        );
    }

    private Cliente mapear(ResultSet resultado) throws SQLException {
        return new Cliente(
                resultado.getInt("id"),
                resultado.getInt("hotel_id"),
                resultado.getString(
                        "nombre_completo"
                ),
                resultado.getString(
                        "documento_identidad"
                ),
                resultado.getString(
                        "telefono"
                )
        );
    }

    private void exigirId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El cliente debe tener id para actualizarse"
            );
        }
    }
}
