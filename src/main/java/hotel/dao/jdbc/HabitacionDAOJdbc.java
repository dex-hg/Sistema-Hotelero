package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.dao.HabitacionDAO;

import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.TipoHabitacion;
import hotel.modelo.sesion.ProveedorHotelId;

import hotel.patrones.creacional.HabitacionBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementacion JDBC de {@link HabitacionDAO} para habitaciones del hotel
 * activo.
 *
 * APLICA PRINCIPIO SOLID: - SRP: concentra la persistencia de habitaciones y no
 * contiene reglas de negocio. - DIP: expone el contrato {@link HabitacionDAO};
 * servicios y controladores no necesitan conocer esta clase concreta.
 */
public final class HabitacionDAOJdbc implements HabitacionDAO {

    private static final String COLUMNAS
            = "id, hotel_id, "
            + "numero, tipo, precio_por_noche, "
            + "cantidad_camas, tiene_bano_privado, tiene_tv, "
            + "estado";
    private final ProveedorHotelId proveedorHotelId;
    private final EjecutorDAO ejecutorDAO;

    public HabitacionDAOJdbc(
            ProveedorConexion proveedorConexion,
            ProveedorHotelId proveedorHotelId
    ) {
        Objects.requireNonNull(proveedorConexion);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
        this.ejecutorDAO = new EjecutorDAO(proveedorConexion);
    }

    @Override
    public Optional<Habitacion> buscarPorId(int id) {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM habitaciones WHERE hotel_id = ? AND id = ?";

        return ejecutorDAO.consultarUno(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId(),
                id
        );
    }

    @Override
    public Optional<Habitacion> buscarPorIdParaActualizar(int id) {
        String sql = "SELECT " + COLUMNAS
                + " FROM habitaciones WHERE hotel_id = ? AND id = ? "
                + "FOR UPDATE";
        return ejecutorDAO.consultarUno(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId(),
                id
        );
    }

    @Override
    public Optional<Habitacion> buscarPorNumero(String numero) {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM habitaciones WHERE hotel_id = ? "
                + "AND numero = ?";

        return ejecutorDAO.consultarUno(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId(),
                numero
        );
    }

    @Override
    public List<Habitacion> listar() {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM habitaciones WHERE hotel_id = ? "
                + "ORDER BY numero";

        return ejecutorDAO.consultarLista(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId()
        );
    }

    @Override
    public Habitacion crear(Habitacion habitacion) {
        String sql
                = "INSERT INTO habitaciones "
                + "(hotel_id, numero, tipo, "
                + "precio_por_noche, cantidad_camas, "
                + "tiene_bano_privado, tiene_tv, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        int id = ejecutorDAO.crearYObtenerId(
                sql,
                proveedorHotelId.getHotelId(),
                habitacion.getNumero(),
                habitacion.getTipo().name(),
                habitacion.getPrecioPorNoche(),
                habitacion.getCantidadCamas(),
                habitacion.tieneBanoPrivado(),
                habitacion.tieneTv(),
                habitacion.getEstado().name()
        );

        return copiarConId(habitacion, id);
    }

    @Override
    public boolean actualizar(Habitacion habitacion) {
        exigirId(habitacion.getId());
        String sql
                = "UPDATE habitaciones "
                + "SET numero = ?, tipo = ?, "
                + "precio_por_noche = ?, cantidad_camas = ?, "
                + "tiene_bano_privado = ?, tiene_tv = ?, estado = ? "
                + "WHERE hotel_id = ? AND id = ?";

        return ejecutorDAO.ejecutarModificacion(
                sql,
                habitacion.getNumero(),
                habitacion.getTipo().name(),
                habitacion.getPrecioPorNoche(),
                habitacion.getCantidadCamas(),
                habitacion.tieneBanoPrivado(),
                habitacion.tieneTv(),
                habitacion.getEstado().name(),
                proveedorHotelId.getHotelId(),
                habitacion.getId()
        );
    }

    @Override
    public boolean eliminar(int id) {
        String sql
                = "DELETE FROM habitaciones "
                + "WHERE hotel_id = ? "
                + "AND id = ?";

        return ejecutorDAO.ejecutarModificacion(
                sql,
                proveedorHotelId.getHotelId(),
                id
        );
    }

    private Habitacion mapear(ResultSet resultado) throws SQLException {
        HabitacionBuilder builder = new HabitacionBuilder()
                .conId(resultado.getInt("id"))
                .paraHotel(resultado.getInt("hotel_id"))
                .conNumero(resultado.getString("numero"))
                .deTipo(TipoHabitacion.valueOf(
                        resultado.getString("tipo"))
                )
                .conPrecioPorNoche(
                        resultado.getBigDecimal(
                                "precio_por_noche"
                        )
                )
                .conCantidadCamas(
                        resultado.getInt(
                                "cantidad_camas"
                        )
                )
                .conEstado(
                        EstadoHabitacion.valueOf(
                                resultado.getString("estado")
                        )
                );

        if (resultado.getBoolean("tiene_bano_privado")) {
            builder.conBanoPrivado();
        }

        if (resultado.getBoolean("tiene_tv")) {
            builder.conTv();
        }
        return builder.construir();
    }

    private Habitacion copiarConId(Habitacion habitacion, int id) {
        HabitacionBuilder builder = new HabitacionBuilder()
                .conId(id)
                .paraHotel(proveedorHotelId.getHotelId())
                .conNumero(habitacion.getNumero())
                .deTipo(habitacion.getTipo())
                .conPrecioPorNoche(
                        habitacion.getPrecioPorNoche()
                )
                .conCantidadCamas(
                        habitacion.getCantidadCamas()
                )
                .conEstado(habitacion.getEstado());

        if (habitacion.tieneBanoPrivado()) {
            builder.conBanoPrivado();
        }

        if (habitacion.tieneTv()) {
            builder.conTv();
        }

        return builder.construir();
    }

    private void exigirId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "La habitacion debe tener id para actualizarse"
            );
        }
    }
}
