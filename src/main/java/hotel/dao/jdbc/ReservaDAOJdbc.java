package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.dao.ReservaDAO;

import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.modelo.sesion.ProveedorHotelId;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementacion JDBC de {@link ReservaDAO} para reservas del tenant activo.
 *
 * APLICA PRINCIPIO SOLID: - SRP: se limita a persistir reservas; las reglas de
 * check-in, check-out y pagos permanecen en la capa de servicio.
 */
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

    private final ProveedorHotelId proveedorHotelId;
    private final EjecutorDAO ejecutorDAO;

    public ReservaDAOJdbc(
            ProveedorConexion proveedorConexion,
            ProveedorHotelId proveedorHotelId
    ) {
        Objects.requireNonNull(proveedorConexion);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
        this.ejecutorDAO = new EjecutorDAO(proveedorConexion);
    }

    @Override
    public Optional<Reserva> buscarPorId(int id) {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM reservas WHERE hotel_id = ? AND id = ?";

        return ejecutorDAO.consultarUno(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId(),
                id
        );
    }

    @Override
    public List<Reserva> listar() {
        String sql
                = "SELECT "
                + COLUMNAS
                + " FROM reservas "
                + "WHERE hotel_id = ? "
                + "ORDER BY fecha_ingreso DESC";

        return ejecutorDAO.consultarLista(
                sql,
                this::mapear,
                proveedorHotelId.getHotelId()
        );
    }

    @Override
    public Reserva crear(Reserva reserva) {
        String sql
                = "INSERT INTO reservas "
                + "(hotel_id, habitacion_id, cliente_id, "
                + "fecha_ingreso, fecha_salida, "
                + "total_pagado, estado_reserva) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        int id = ejecutorDAO.crearYObtenerId(sql,
                proveedorHotelId.getHotelId(),
                reserva.getHabitacionId(),
                reserva.getClienteId(),
                reserva.getFechaIngreso(),
                reserva.getFechaSalida(),
                reserva.getTotalPagado(),
                reserva.getEstado().name()
        );

        return new Reserva(
                id,
                proveedorHotelId.getHotelId(),
                reserva.getHabitacionId(),
                reserva.getClienteId(),
                reserva.getFechaIngreso(),
                reserva.getFechaSalida(),
                reserva.getTotalPagado(),
                reserva.getEstado()
        );
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

        return ejecutorDAO.ejecutarModificacion(
                sql,
                reserva.getHabitacionId(),
                reserva.getClienteId(),
                reserva.getFechaIngreso(),
                reserva.getFechaSalida(),
                reserva.getTotalPagado(),
                reserva.getEstado().name(),
                proveedorHotelId.getHotelId(),
                reserva.getId()
        );
    }

    @Override
    public boolean eliminar(int id) {
        String sql
                = "DELETE FROM reservas "
                + "WHERE hotel_id = ? "
                + "AND id = ?";

        return ejecutorDAO.ejecutarModificacion(
                sql,
                proveedorHotelId.getHotelId(),
                id
        );
    }

    private Reserva mapear(ResultSet resultado) throws SQLException {
        return new Reserva(
                resultado.getInt("id"),
                resultado.getInt("hotel_id"),
                resultado.getInt("habitacion_id"),
                resultado.getInt("cliente_id"),
                resultado.getTimestamp("fecha_ingreso")
                        .toLocalDateTime(),
                resultado.getTimestamp("fecha_salida")
                        .toLocalDateTime(),
                resultado.getBigDecimal("total_pagado"),
                EstadoReserva.valueOf(
                        resultado.getString("estado_reserva")
                )
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
