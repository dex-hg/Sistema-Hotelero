package hotel.patrones.estructural;

import hotel.dao.ClienteDAO;

import hotel.modelo.entidades.Cliente;
import hotel.modelo.sesion.ProveedorHotelId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import hotel.excepcion.AccesoTenantException;

/**
 * Proxy de proteccion para {@link ClienteDAO}.
 *
 * PATRON DE DISENO:
 * - Proxy: intercepta el acceso al DAO real y valida que exista sesion y que
 *   las entidades pertenezcan al hotel activo antes de delegar.
 *
 * APLICA PRINCIPIO SOLID:
 * - LSP: puede usarse donde se espera un {@link ClienteDAO} sin cambiar el
 *   contrato de los metodos.
 * - SRP: su unica responsabilidad es proteger el limite multi-tenant.
 */
public final class ClienteDAOProxy implements ClienteDAO {

    private final ClienteDAO daoReal;
    private final ProveedorHotelId proveedorHotelId;

    public ClienteDAOProxy(
            ClienteDAO daoReal,
            ProveedorHotelId proveedorHotelId
    ) {
        this.daoReal = Objects.requireNonNull(daoReal);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        exigirSesion();
        return daoReal.buscarPorId(id);
    }

    @Override
    public Optional<Cliente> buscarPorIdParaActualizar(int id) {
        exigirSesion();
        return daoReal.buscarPorIdParaActualizar(id);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documentoIdentidad) {
        exigirSesion();
        return daoReal.buscarPorDocumento(documentoIdentidad);
    }

    @Override
    public List<Cliente> listar() {
        exigirSesion();
        return daoReal.listar();
    }

    @Override
    public Cliente crear(Cliente cliente) {
        exigirMismoHotel(cliente.getHotelId());
        return daoReal.crear(cliente);
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        exigirMismoHotel(cliente.getHotelId());
        return daoReal.actualizar(cliente);
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
