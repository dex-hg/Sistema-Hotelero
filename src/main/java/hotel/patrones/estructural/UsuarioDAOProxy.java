package hotel.patrones.estructural;

import hotel.dao.UsuarioDAO;

import hotel.modelo.entidades.Usuario;
import hotel.modelo.sesion.ProveedorHotelId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class UsuarioDAOProxy implements UsuarioDAO {

    private final UsuarioDAO daoReal;
    private final ProveedorHotelId proveedorHotelId;

    public UsuarioDAOProxy(
            UsuarioDAO daoReal,
            ProveedorHotelId proveedorHotelId
    ) {
        this.daoReal = Objects.requireNonNull(daoReal);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
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
        exigirMismoHotel(usuario.getHotelId());
        return daoReal.crear(usuario);
    }

    @Override
    public boolean actualizar(Usuario usuario) {
        exigirMismoHotel(usuario.getHotelId());
        return daoReal.actualizar(usuario);
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
