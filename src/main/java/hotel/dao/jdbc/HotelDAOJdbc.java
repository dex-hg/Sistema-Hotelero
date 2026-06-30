package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.dao.HotelDAO;

import hotel.modelo.entidades.Hotel;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * DAO JDBC de hoteles, raiz del modelo multi-tenant.
 *
 * APLICA PRINCIPIO SOLID: - SRP: administra la persistencia de hoteles sin
 * mezclar autenticacion, sesion ni reglas de negocio.
 */
public final class HotelDAOJdbc implements HotelDAO {

    private final EjecutorDAO ejecutorDAO;

    public HotelDAOJdbc(ProveedorConexion proveedorConexion) {
        Objects.requireNonNull(proveedorConexion);
        this.ejecutorDAO = new EjecutorDAO(proveedorConexion);
    }

    @Override
    public Optional<Hotel> buscarPorId(int id) {
        String sql = "SELECT * FROM hoteles WHERE id = ?";
        return ejecutorDAO.consultarUno(sql, this::mapear, id);
    }

    @Override
    public Optional<Hotel> buscarPorRuc(String ruc) {
        String sql = "SELECT * FROM hoteles WHERE ruc = ?";
        return ejecutorDAO.consultarUno(sql, this::mapear, ruc);
    }

    @Override
    public List<Hotel> listar() {
        String sql = "SELECT * FROM hoteles ORDER BY nombre";
        return ejecutorDAO.consultarLista(sql, this::mapear);
    }

    @Override
    public Hotel crear(Hotel hotel) {
        String sql
                = "INSERT INTO hoteles "
                + "(nombre, ruc, direccion, creado_en) "
                + "VALUES (?, ?, ?, ?)";

        int id = ejecutorDAO.crearYObtenerId(sql,
                hotel.getNombre(),
                hotel.getRuc(),
                hotel.getDireccion(),
                hotel.getCreadoEn()
        );

        return new Hotel(
                id,
                hotel.getNombre(),
                hotel.getRuc(),
                hotel.getDireccion(),
                hotel.getCreadoEn()
        );
    }

    @Override
    public boolean actualizar(Hotel hotel) {
        exigirId(hotel.getId());
        String sql
                = "UPDATE hoteles SET nombre = ?, ruc = ?, direccion = ? "
                + "WHERE id = ?";

        return ejecutorDAO.ejecutarModificacion(
                sql,
                hotel.getNombre(),
                hotel.getRuc(),
                hotel.getDireccion(),
                hotel.getId()
        );
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM hoteles WHERE id = ?";
        return ejecutorDAO.ejecutarModificacion(sql, id);
    }

    private Hotel mapear(ResultSet resultado) throws SQLException {
        return new Hotel(
                resultado.getInt("id"),
                resultado.getString("nombre"),
                resultado.getString("ruc"),
                resultado.getString("direccion"),
                resultado.getTimestamp("creado_en")
                        .toLocalDateTime()
        );
    }

    private void exigirId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El hotel debe tener id para actualizarse"
            );
        }
    }
}
