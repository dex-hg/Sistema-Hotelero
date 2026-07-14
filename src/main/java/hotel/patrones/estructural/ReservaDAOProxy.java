package hotel.patrones.estructural;

import hotel.dao.ReservaDAO;

import hotel.modelo.entidades.Cliente;
import hotel.modelo.entidades.Reserva;
import hotel.modelo.sesion.ProveedorHotelId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;

import hotel.excepcion.AccesoTenantException;

/**
 * Proxy de seguridad y aislamiento multi-tenant para {@link ReservaDAO}.
 * Intercepta todas las llamadas al DAO real para asegurar que el usuario solo
 * acceda a reservas que pertenecen a su hotel (tenant activo).
 *
 * PATRÓN DE DISEÑO: - Proxy: Actúa como un proxy de protección (Protection
 * Proxy), controlando el acceso a los métodos del DAO real mediante
 * verificaciones de seguridad.
 *
 * APLICA PRINCIPIO SOLID: - LSP (Liskov Substitution Principle): Esta clase
 * implementa la interfaz {@link ReservaDAO} y puede sustituir transparentemente
 * a la implementación real ({@code ReservaDAOJdbc}) en cualquier cliente que
 * dependa de la interfaz, sin alterar la semántica básica de los métodos. - SRP
 * (Single Responsibility Principle): Su única responsabilidad es aplicar y
 * garantizar la seguridad y aislamiento multi-tenant del acceso a base de
 * datos.
 */
public final class ReservaDAOProxy implements ReservaDAO {

    private final ReservaDAO daoReal;
    private final ProveedorHotelId proveedorHotelId;

    public ReservaDAOProxy(
            ReservaDAO daoReal,
            ProveedorHotelId proveedorHotelId
    ) {
        this.daoReal = Objects.requireNonNull(daoReal);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
    }

    @Override
    public Optional<Reserva> buscarPorId(int id) {
        exigirSesion();
        return daoReal.buscarPorId(id);
    }

    @Override
    public List<Reserva> listar() {
        exigirSesion();
        return daoReal.listar();
    }

    @Override
    public Reserva crear(Reserva reserva) {
        exigirMismoHotel(reserva.getHotelId());
        return daoReal.crear(reserva);
    }

    @Override
    public void asociarHuesped(
            int reservaId,
            int clienteId,
            boolean principal
    ) {
        exigirSesion();
        daoReal.asociarHuesped(reservaId, clienteId, principal);
    }

    @Override
    public List<Cliente> listarHuespedes(int reservaId) {
        exigirSesion();
        return daoReal.listarHuespedes(reservaId);
    }

    @Override
    public Map<Integer, List<Cliente>> listarHuespedesPorReserva() {
        exigirSesion();
        return daoReal.listarHuespedesPorReserva();
    }

    @Override
    public boolean existeReservaActivaParaCliente(int clienteId) {
        exigirSesion();
        return daoReal.existeReservaActivaParaCliente(clienteId);
    }

    @Override
    public boolean actualizar(Reserva reserva) {
        exigirMismoHotel(reserva.getHotelId());
        return daoReal.actualizar(reserva);
    }

    @Override
    public boolean eliminar(int id) {
        exigirSesion();
        return daoReal.eliminar(id);
    }

    private void exigirSesion() {
        proveedorHotelId.getHotelId();
    }

    private void exigirMismoHotel(int hotelId) {
        if (hotelId != proveedorHotelId.getHotelId()) {
            throw new AccesoTenantException();
        }
    }
}
