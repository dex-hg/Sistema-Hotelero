package hotel.patrones.estructural;

import hotel.dao.UsuarioDAO;

import hotel.modelo.entidades.Usuario;
import hotel.modelo.sesion.ProveedorHotelId;
import hotel.modelo.seguridad.AutorizadorAcceso;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import hotel.excepcion.AccesoTenantException;

/**
 * Proxy de proteccion para {@link UsuarioDAO}.
 *
 * PATRON DE DISENO: - Proxy: controla el acceso al DAO de usuarios para impedir
 * operaciones fuera del tenant actual.
 *
 * APLICA PRINCIPIO SOLID: - LSP: mantiene el mismo contrato de
 * {@link UsuarioDAO}. - SRP: encapsula la politica de seguridad multi-tenant de
 * usuarios.
 */
public final class UsuarioDAOProxy implements UsuarioDAO {

    private final UsuarioDAO daoReal;
    private final ProveedorHotelId proveedorHotelId;
    private final AutorizadorAcceso autorizadorAcceso;

    public UsuarioDAOProxy(
            UsuarioDAO daoReal,
            ProveedorHotelId proveedorHotelId,
            AutorizadorAcceso autorizadorAcceso
    ) {
        this.daoReal = Objects.requireNonNull(daoReal);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
        this.autorizadorAcceso = Objects.requireNonNull(autorizadorAcceso);
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        exigirSesion();
        return daoReal.buscarPorId(id);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        exigirSesion();
        return daoReal.buscarPorUsername(username);
    }

    @Override
    public List<Usuario> listar() {
        exigirSesion();
        return daoReal.listar();
    }

    @Override
    public Usuario crear(Usuario usuario) {
        autorizadorAcceso.exigirAdministrador();
        exigirMismoHotel(usuario.getHotelId());
        return daoReal.crear(usuario);
    }

    @Override
    public boolean actualizar(Usuario usuario) {
        autorizadorAcceso.exigirAdministrador();
        exigirMismoHotel(usuario.getHotelId());
        return daoReal.actualizar(usuario);
    }

    @Override
    public boolean eliminar(int id) {
        autorizadorAcceso.exigirAdministrador();
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
