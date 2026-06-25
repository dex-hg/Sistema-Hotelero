package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.excepcion.DAOException;
import hotel.dao.HabitacionDAO;

import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.TipoHabitacion;
import hotel.modelo.sesion.ProveedorHotelId;

import hotel.patrones.creacional.HabitacionBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class HabitacionDAOJdbc implements HabitacionDAO {

    private static final String COLUMNAS = "id, hotel_id, numero, tipo, precio_por_noche, "
            + "cantidad_camas, tiene_bano_privado, tiene_tv, estado";
    private final ProveedorConexion proveedorConexion;
    private final ProveedorHotelId proveedorHotelId;

    public HabitacionDAOJdbc(ProveedorConexion proveedorConexion, ProveedorHotelId proveedorHotelId) {
        this.proveedorConexion = Objects.requireNonNull(proveedorConexion);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
    }

    @Override
    public Optional<Habitacion> buscarPorId(int id) {
        return buscarUno("SELECT " + COLUMNAS + " FROM habitaciones WHERE hotel_id = ? AND id = ?", id);
    }

    @Override
    public Optional<Habitacion> buscarPorNumero(String numero) {
        String sql = "SELECT " + COLUMNAS + " FROM habitaciones WHERE hotel_id = ? AND numero = ?";
        try (Connection conexion = proveedorConexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());
            sentencia.setString(2, numero);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar la habitacion", e);
        }
    }

    @Override
    public List<Habitacion> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM habitaciones WHERE hotel_id = ? ORDER BY numero";
        List<Habitacion> habitaciones = new ArrayList<>();
        try (Connection conexion = proveedorConexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) habitaciones.add(mapear(resultado));
            }
            return habitaciones;
        } catch (SQLException e) {
            throw new DAOException("No se pudieron listar las habitaciones", e);
        }
    }

    @Override
    public Habitacion crear(Habitacion habitacion) {
        String sql = "INSERT INTO habitaciones (hotel_id, numero, tipo, precio_por_noche, cantidad_camas, "
                + "tiene_bano_privado, tiene_tv, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = proveedorConexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarCampos(sentencia, habitacion);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) throw new DAOException("PostgreSQL no devolvio el id de la habitacion");
                return copiarConId(habitacion, claves.getInt(1));
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo crear la habitacion", e);
        }
    }

    @Override
    public boolean actualizar(Habitacion habitacion) {
        exigirId(habitacion.getId());
        String sql = "UPDATE habitaciones SET numero = ?, tipo = ?, precio_por_noche = ?, cantidad_camas = ?, "
                + "tiene_bano_privado = ?, tiene_tv = ?, estado = ? WHERE hotel_id = ? AND id = ?";
        try (Connection conexion = proveedorConexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, habitacion.getNumero());
            sentencia.setString(2, habitacion.getTipo().name());
            sentencia.setBigDecimal(3, habitacion.getPrecioPorNoche());
            sentencia.setInt(4, habitacion.getCantidadCamas());
            sentencia.setBoolean(5, habitacion.tieneBanoPrivado());
            sentencia.setBoolean(6, habitacion.tieneTv());
            sentencia.setString(7, habitacion.getEstado().name());
            sentencia.setInt(8, proveedorHotelId.getHotelId());
            sentencia.setInt(9, habitacion.getId());
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("No se pudo actualizar la habitacion", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM habitaciones WHERE hotel_id = ? AND id = ?";
        try (Connection conexion = proveedorConexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());
            sentencia.setInt(2, id);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("No se pudo eliminar la habitacion", e);
        }
    }

    private Optional<Habitacion> buscarUno(String sql, int id) {
        try (Connection conexion = proveedorConexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());
            sentencia.setInt(2, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar la habitacion", e);
        }
    }

    private void asignarCampos(PreparedStatement sentencia, Habitacion habitacion)
            throws SQLException {
        sentencia.setInt(1, proveedorHotelId.getHotelId());
        sentencia.setString(2, habitacion.getNumero());
        sentencia.setString(3, habitacion.getTipo().name());
        sentencia.setBigDecimal(4, habitacion.getPrecioPorNoche());
        sentencia.setInt(5, habitacion.getCantidadCamas());
        sentencia.setBoolean(6, habitacion.tieneBanoPrivado());
        sentencia.setBoolean(7, habitacion.tieneTv());
        sentencia.setString(8, habitacion.getEstado().name());
    }

    private Habitacion mapear(ResultSet resultado) throws SQLException {
        HabitacionBuilder builder = new HabitacionBuilder()
                .conId(resultado.getInt("id"))
                .paraHotel(resultado.getInt("hotel_id"))
                .conNumero(resultado.getString("numero"))
                .deTipo(TipoHabitacion.valueOf(resultado.getString("tipo")))
                .conPrecioPorNoche(resultado.getBigDecimal("precio_por_noche"))
                .conCantidadCamas(resultado.getInt("cantidad_camas"))
                .conEstado(EstadoHabitacion.valueOf(resultado.getString("estado")));
        if (resultado.getBoolean("tiene_bano_privado")) builder.conBanoPrivado();
        if (resultado.getBoolean("tiene_tv")) builder.conTv();
        return builder.construir();
    }

    private Habitacion copiarConId(Habitacion habitacion, int id) {
        HabitacionBuilder builder = new HabitacionBuilder()
                .conId(id).paraHotel(proveedorHotelId.getHotelId()).conNumero(habitacion.getNumero())
                .deTipo(habitacion.getTipo()).conPrecioPorNoche(habitacion.getPrecioPorNoche())
                .conCantidadCamas(habitacion.getCantidadCamas()).conEstado(habitacion.getEstado());
        if (habitacion.tieneBanoPrivado()) builder.conBanoPrivado();
        if (habitacion.tieneTv()) builder.conTv();
        return builder.construir();
    }

    private void exigirId(Integer id) {
        if (id == null) throw new IllegalArgumentException("La habitacion debe tener id para actualizarse");
    }
}
