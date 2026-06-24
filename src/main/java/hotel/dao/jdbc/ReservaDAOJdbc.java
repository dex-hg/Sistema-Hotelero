package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.dao.DAOException;
import hotel.dao.ReservaDAO;

import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.modelo.sesion.ProveedorHotelId;

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

public final class ReservaDAOJdbc implements ReservaDAO {

    private static final String COLUMNAS
            = "id, "
            + "hotel_id, "
            + "habitacion_id, "
            + "cliente_id, "
            + "fecha_ingreso, "
            + "fecha_salida, "
            + "total_pagado, "
            + "estado_reserva";

    private final ProveedorConexion proveedorConexion;
    private final ProveedorHotelId proveedorHotelId;

    public ReservaDAOJdbc(ProveedorConexion proveedorConexion, ProveedorHotelId proveedorHotelId) {
        this.proveedorConexion = Objects.requireNonNull(proveedorConexion);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
    }

    @Override
    public Optional<Reserva> buscarPorId(int id) {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM reservas "
                + "WHERE hotel_id = ? "
                + "AND id = ?";

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
            throw new DAOException("No se pudo buscar la reserva", e);
        }
    }

    @Override
    public List<Reserva> listar() {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM reservas "
                + "WHERE hotel_id = ? "
                + "ORDER BY fecha_ingreso DESC";

        List<Reserva> reservas = new ArrayList<>();

        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    reservas.add(mapear(resultado));
                }
            }

            return reservas;

        } catch (SQLException e) {
            throw new DAOException("No se pudieron listar las reservas", e);
        }
    }

    @Override
    public Reserva crear(Reserva reserva) {
        String sql
                = "INSERT INTO reservas "
                + "(hotel_id, habitacion_id, cliente_id, "
                + "fecha_ingreso, fecha_salida, "
                + "total_pagado, estado_reserva) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());
            sentencia.setInt(2, reserva.getHabitacionId());
            sentencia.setInt(3, reserva.getClienteId());
            sentencia.setTimestamp(4, Timestamp.valueOf(reserva.getFechaIngreso()));
            sentencia.setTimestamp(5, Timestamp.valueOf(reserva.getFechaSalida()));
            sentencia.setBigDecimal(6, reserva.getTotalPagado());
            sentencia.setString(7, reserva.getEstado().name());
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new DAOException("PostgreSQL no devolvio el id de la reserva");
                }

                return new Reserva(
                        claves.getInt(1),
                        proveedorHotelId.getHotelId(),
                        reserva.getHabitacionId(),
                        reserva.getClienteId(),
                        reserva.getFechaIngreso(),
                        reserva.getFechaSalida(),
                        reserva.getTotalPagado(),
                        reserva.getEstado()
                );
            }

        } catch (SQLException e) {
            throw new DAOException("No se pudo crear la reserva", e);
        }
    }

    @Override
    public boolean actualizar(Reserva reserva) {
        exigirId(reserva.getId());
        String sql
                = "UPDATE reservas "
                + "SET habitacion_id = ?, cliente_id = ?, "
                + "fecha_ingreso = ?, fecha_salida = ?, "
                + "total_pagado = ?, estado_reserva = ? "
                + "WHERE hotel_id = ? "
                + "AND id = ?";

        try (
                Connection conexion = proveedorConexion.obtenerConexion(); 
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, reserva.getHabitacionId());
            sentencia.setInt(2, reserva.getClienteId());
            sentencia.setTimestamp(3, Timestamp.valueOf(reserva.getFechaIngreso()));
            sentencia.setTimestamp(4, Timestamp.valueOf(reserva.getFechaSalida()));
            sentencia.setBigDecimal(5, reserva.getTotalPagado());
            sentencia.setString(6, reserva.getEstado().name());
            sentencia.setInt(7, proveedorHotelId.getHotelId());
            sentencia.setInt(8, reserva.getId());
            
            return sentencia.executeUpdate() == 1;
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo actualizar la reserva", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM reservas WHERE hotel_id = ? AND id = ?";
        try (
                Connection conexion = proveedorConexion.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, proveedorHotelId.getHotelId());
            sentencia.setInt(2, id);
            
            return sentencia.executeUpdate() == 1;
            
        } catch (SQLException e) {
            throw new DAOException("No se pudo eliminar la reserva", e);
        }
    }

    private Reserva mapear(ResultSet resultado) throws SQLException {
        return new Reserva(
                resultado.getInt("id"),
                resultado.getInt("hotel_id"),
                resultado.getInt("habitacion_id"),
                resultado.getInt("cliente_id"),
                resultado.getTimestamp("fecha_ingreso").toLocalDateTime(),
                resultado.getTimestamp("fecha_salida").toLocalDateTime(),
                resultado.getBigDecimal("total_pagado"),
                EstadoReserva.valueOf(resultado.getString("estado_reserva"))
        );
    }

    private void exigirId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "La reserva debe tener id para actualizarse"
            );
        }
    }
}
