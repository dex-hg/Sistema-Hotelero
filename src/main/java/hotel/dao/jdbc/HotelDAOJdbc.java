package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.excepcion.DAOException;
import hotel.dao.HotelDAO;

import hotel.modelo.entidades.Hotel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class HotelDAOJdbc implements HotelDAO {

    private final ProveedorConexion proveedorConexion;

    public HotelDAOJdbc(ProveedorConexion proveedorConexion) {
        this.proveedorConexion = Objects.requireNonNull(proveedorConexion);
    }

    @Override
    public Optional<Hotel> buscarPorId(int id) {
        return buscarUno(
                "SELECT * FROM hoteles WHERE id = ?",
                id
        );
    }

    @Override
    public Optional<Hotel> buscarPorRuc(String ruc) {
        String sql = "SELECT * FROM hoteles WHERE ruc = ?";

        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, ruc);
            
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el hotel por RUC", e);
        }
    }

    @Override
    public List<Hotel> listar() {
        String sql = "SELECT * FROM hoteles ORDER BY nombre";
        List<Hotel> hoteles = new ArrayList<>();
        
        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()
        ) {
            while (resultado.next()) {
                hoteles.add(mapear(resultado));
            }
            
            return hoteles;
            
        } catch (SQLException e) {
            throw new DAOException("No se pudieron listar los hoteles", e);
        }
    }

    @Override
    public Hotel crear(Hotel hotel) {
        String sql 
                = "INSERT INTO hoteles "
                + "(nombre, ruc, direccion, creado_en) "
                + "VALUES (?, ?, ?, ?)";
        
        try (
                Connection conexion = proveedorConexion.obtenerConexion(); 
                PreparedStatement sentencia = conexion.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            sentencia.setString(1, hotel.getNombre());
            sentencia.setString(2, hotel.getRuc());
            sentencia.setString(3, hotel.getDireccion());
            sentencia.setTimestamp(4, Timestamp.valueOf(hotel.getCreadoEn()));
            sentencia.executeUpdate();
            
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new DAOException(
                            "PostgreSQL no devolvio el id del hotel"
                    );
                }
                return new Hotel(
                        claves.getInt(1),
                        hotel.getNombre(),
                        hotel.getRuc(),
                        hotel.getDireccion(),
                        hotel.getCreadoEn());
            }
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo crear el hotel", e);
        }
    }

    @Override
    public boolean actualizar(Hotel hotel) {
        exigirId(hotel.getId());
        String sql 
                = "UPDATE hoteles "
                + "SET nombre = ?, "
                + "ruc = ?, "
                + "direccion = ? "
                + "WHERE id = ?";
        
        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, hotel.getNombre());
            sentencia.setString(2, hotel.getRuc());
            sentencia.setString(3, hotel.getDireccion());
            sentencia.setInt(4, hotel.getId());
            
            return sentencia.executeUpdate() == 1;
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo actualizar el hotel", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM hoteles WHERE id = ?";
        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, id);
            
            return sentencia.executeUpdate() == 1;
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo eliminar el hotel", e);
        }
    }

    private Optional<Hotel> buscarUno(String sql, int id) {
        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, id);
            
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el hotel", e);
        }
    }

    private Hotel mapear(ResultSet resultado) throws SQLException {
        return new Hotel(
                resultado.getInt("id"),
                resultado.getString("nombre"),
                resultado.getString("ruc"),
                resultado.getString("direccion"),
                resultado.getTimestamp("creado_en").toLocalDateTime()
        );
    }

    private void exigirId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El hotel debe tener id para actualizarse");
        }
    }
}
