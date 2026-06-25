package hotel.patrones.estructural;

import hotel.dao.ReservaDAO;

import hotel.modelo.entidades.Reserva;
import hotel.modelo.sesion.ProveedorHotelId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import hotel.excepcion.AccesoTenantException;

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
