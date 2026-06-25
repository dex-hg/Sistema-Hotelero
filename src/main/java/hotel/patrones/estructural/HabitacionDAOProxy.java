package hotel.patrones.estructural;

import hotel.dao.HabitacionDAO;

import hotel.modelo.entidades.Habitacion;
import hotel.modelo.sesion.ProveedorHotelId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import hotel.excepcion.AccesoTenantException;

public final class HabitacionDAOProxy implements HabitacionDAO {

    private final HabitacionDAO daoReal;
    private final ProveedorHotelId proveedorHotelId;

    public HabitacionDAOProxy(
            HabitacionDAO daoReal,
            ProveedorHotelId proveedorHotelId
    ) {
        this.daoReal = Objects.requireNonNull(daoReal);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
    }

    @Override
    public Optional<Habitacion> buscarPorId(int id) {
        exigirSesion();
        return daoReal.buscarPorId(id);
    }

    @Override
    public Optional<Habitacion> buscarPorNumero(String numero) {
        exigirSesion();
        return daoReal.buscarPorNumero(numero);
    }

    @Override
    public List<Habitacion> listar() {
        exigirSesion();
        return daoReal.listar();
    }

    @Override
    public Habitacion crear(Habitacion habitacion) {
        exigirMismoHotel(habitacion.getHotelId());
        return daoReal.crear(habitacion);
    }

    @Override
    public boolean actualizar(Habitacion habitacion) {
        exigirMismoHotel(habitacion.getHotelId());
        return daoReal.actualizar(habitacion);
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
