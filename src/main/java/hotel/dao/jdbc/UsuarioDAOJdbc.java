package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.dao.DAOException;
import hotel.dao.UsuarioDAO;

import hotel.modelo.entidades.Usuario;
import hotel.modelo.entidades.constantes.RolUsuario;
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

public final class UsuarioDAOJdbc implements UsuarioDAO {

    private static final String COLUMNAS = "id, hotel_id, username, password, rol";
    private final ProveedorConexion proveedorConexion;
    private final ProveedorHotelId proveedorHotelId;

    public UsuarioDAOJdbc(
            ProveedorConexion proveedorConexion,
            ProveedorHotelId proveedorHotelId
    ) {
        this.proveedorConexion = Objects.requireNonNull(proveedorConexion);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        return buscarUno(
                "SELECT "
                + COLUMNAS
                + " FROM usuarios WHERE hotel_id = ? AND id = ?",
                id);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        String sql 
                = "SELECT " 
                + COLUMNAS 
                + " FROM usuarios WHERE hotel_id = ? "
                + "AND username = ?";
        
        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());
            sentencia.setString(2, username);
            
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el usuario", e);
        }
    }

    @Override
    public List<Usuario> listar() {
        String sql
                = "SELECT " 
                + COLUMNAS
                + " FROM usuarios WHERE hotel_id = ? "
                + "ORDER BY username";
        
        List<Usuario> usuarios = new ArrayList<>();
        
        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());
            
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    usuarios.add(mapear(resultado));
                }
            }
            
            return usuarios;
            
        } catch (SQLException e) {
            throw new DAOException("No se pudieron listar los usuarios", e);
        }
    }

    @Override
    public Usuario crear(Usuario usuario) {
        String sql 
                = "INSERT INTO usuarios "
                + "(hotel_id, username, password, rol) "
                + "VALUES (?, ?, ?, ?)";
        
        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());
            sentencia.setString(2, usuario.getUsername());
            sentencia.setString(3, usuario.getPassword());
            sentencia.setString(4, usuario.getRol().name());
            sentencia.executeUpdate();
            
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new DAOException("PostgreSQL no devolvio el id del usuario");
                }
                
                return new Usuario(
                        claves.getInt(1),
                        proveedorHotelId.getHotelId(),
                        usuario.getUsername(),
                        usuario.getPassword(),
                        usuario.getRol());
            }
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo crear el usuario", e);
        }
    }

    @Override
    public boolean actualizar(Usuario usuario) {
        exigirId(usuario.getId());
        String sql 
                = "UPDATE usuarios "
                + "SET username = ?, password = ?, rol = ? "
                + "WHERE hotel_id = ? "
                + "AND id = ?";
        
        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, usuario.getUsername());
            sentencia.setString(2, usuario.getPassword());
            sentencia.setString(3, usuario.getRol().name());
            sentencia.setInt(4, proveedorHotelId.getHotelId());
            sentencia.setInt(5, usuario.getId());
            
            return sentencia.executeUpdate() == 1;
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo actualizar el usuario", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        return ejecutarEliminacion(
                "DELETE FROM usuarios WHERE hotel_id = ? AND id = ?",
                id
        );
    }

    private Optional<Usuario> buscarUno(String sql, int id) {
        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());
            sentencia.setInt(2, id);
            
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el usuario", e);
        }
    }

    private boolean ejecutarEliminacion(String sql, int id) {
        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());
            sentencia.setInt(2, id);
            
            return sentencia.executeUpdate() == 1;
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo eliminar el usuario", e);
        }
    }

    private Usuario mapear(ResultSet resultado) throws SQLException {
        return new Usuario(
                resultado.getInt("id"),
                resultado.getInt("hotel_id"),
                resultado.getString("username"),
                resultado.getString("password"),
                RolUsuario.valueOf(resultado.getString("rol"))
        );
    }

    private void exigirId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El usuario debe tener id para actualizarse");
        }
    }
}
