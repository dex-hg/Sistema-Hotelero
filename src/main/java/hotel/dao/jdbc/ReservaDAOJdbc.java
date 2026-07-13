package hotel.dao.jdbc;

import hotel.conexion.ProveedorConexion;

import hotel.dao.ReservaDAO;

import hotel.modelo.entidades.Cliente;
import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.modelo.sesion.ProveedorHotelId;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;

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
            + "total_hospedaje, "
            + "monto_pagado, "
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
                + "total_hospedaje, monto_pagado, estado_reserva) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        int id = ejecutorDAO.crearYObtenerId(sql,
                proveedorHotelId.getHotelId(),
                reserva.getHabitacionId(),
                reserva.getClienteId(),
                reserva.getFechaIngreso(),
                reserva.getFechaSalida(),
                reserva.getTotalHospedaje(),
                reserva.getMontoPagado(),
                reserva.getEstado().name()
        );

        return new Reserva(
                id,
                proveedorHotelId.getHotelId(),
                reserva.getHabitacionId(),
                reserva.getClienteId(),
                reserva.getFechaIngreso(),
                reserva.getFechaSalida(),
                reserva.getTotalHospedaje(),
                reserva.getMontoPagado(),
                reserva.getEstado()
        );
    }

    @Override
    public void asociarHuesped(
            int reservaId,
            int clienteId,
            boolean principal
    ) {
        String sql
                = "INSERT INTO reserva_huespedes "
                + "(hotel_id, reserva_id, cliente_id, principal) "
                + "VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (hotel_id, reserva_id, cliente_id) "
                + "DO UPDATE SET principal = EXCLUDED.principal";

        ejecutorDAO.ejecutarModificacion(
                sql,
                proveedorHotelId.getHotelId(),
                reservaId,
                clienteId,
                principal
        );
    }

    @Override
    public List<Cliente> listarHuespedes(int reservaId) {
        String sql
                = "SELECT c.id, c.hotel_id, c.nombre_completo, "
                + "c.documento_identidad, c.telefono "
                + "FROM reserva_huespedes rh "
                + "JOIN clientes c "
                + "ON c.hotel_id = rh.hotel_id "
                + "AND c.id = rh.cliente_id "
                + "WHERE rh.hotel_id = ? "
                + "AND rh.reserva_id = ? "
                + "ORDER BY rh.principal DESC, c.nombre_completo";

        return ejecutorDAO.consultarLista(
                sql,
                this::mapearCliente,
                proveedorHotelId.getHotelId(),
                reservaId
        );
    }

    @Override
    public Map<Integer, List<Cliente>> listarHuespedesPorReserva() {
        String sql = "SELECT rh.reserva_id, c.id, c.hotel_id, "
                + "c.nombre_completo, c.documento_identidad, c.telefono "
                + "FROM reserva_huespedes rh "
                + "JOIN clientes c ON c.hotel_id = rh.hotel_id "
                + "AND c.id = rh.cliente_id "
                + "WHERE rh.hotel_id = ? "
                + "ORDER BY rh.reserva_id, rh.principal DESC, "
                + "c.nombre_completo";
        List<AsociacionHuesped> asociaciones = ejecutorDAO.consultarLista(
                sql,
                resultado -> new AsociacionHuesped(
                        resultado.getInt("reserva_id"),
                        mapearCliente(resultado)
                ),
                proveedorHotelId.getHotelId()
        );
        Map<Integer, List<Cliente>> resultado = new LinkedHashMap<>();
        for (AsociacionHuesped asociacion : asociaciones) {
            resultado.computeIfAbsent(
                    asociacion.reservaId(),
                    id -> new java.util.ArrayList<>()
            ).add(asociacion.cliente());
        }
        return resultado;
    }

    @Override
    public boolean existeReservaActivaParaCliente(int clienteId) {
        String sql = "SELECT EXISTS ("
                + "SELECT 1 FROM reservas r "
                + "WHERE r.hotel_id = ? "
                + "AND r.estado_reserva = 'ACTIVA' "
                + "AND (r.cliente_id = ? OR EXISTS ("
                + "SELECT 1 FROM reserva_huespedes rh "
                + "WHERE rh.hotel_id = r.hotel_id "
                + "AND rh.reserva_id = r.id "
                + "AND rh.cliente_id = ?))) AS existe";

        return ejecutorDAO.consultarUno(
                sql,
                resultado -> resultado.getBoolean("existe"),
                proveedorHotelId.getHotelId(),
                clienteId,
                clienteId
        ).orElse(false);
    }

    @Override
    public boolean actualizar(Reserva reserva) {
        exigirId(reserva.getId());
        String sql
                = "UPDATE reservas "
                + "SET habitacion_id = ?, cliente_id = ?, "
                + "fecha_ingreso = ?, fecha_salida = ?, "
                + "total_hospedaje = ?, monto_pagado = ?, "
                + "estado_reserva = ? "
                + "WHERE hotel_id = ? "
                + "AND id = ?";

        return ejecutorDAO.ejecutarModificacion(
                sql,
                reserva.getHabitacionId(),
                reserva.getClienteId(),
                reserva.getFechaIngreso(),
                reserva.getFechaSalida(),
                reserva.getTotalHospedaje(),
                reserva.getMontoPagado(),
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
                resultado.getBigDecimal("total_hospedaje"),
                resultado.getBigDecimal("monto_pagado"),
                EstadoReserva.valueOf(
                        resultado.getString("estado_reserva")
                )
        );
    }

    private Cliente mapearCliente(ResultSet resultado) throws SQLException {
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
            throw new IllegalArgumentException(
                    "La reserva debe tener id para actualizarse"
            );
        }
    }

    private record AsociacionHuesped(int reservaId, Cliente cliente) {
    }
}
