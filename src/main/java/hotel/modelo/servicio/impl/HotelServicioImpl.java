package hotel.modelo.servicio.impl;

import hotel.dao.HotelDAO;

import hotel.modelo.entidades.Hotel;
import hotel.modelo.servicio.HotelServicio;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class HotelServicioImpl implements HotelServicio {

    private final HotelDAO hotelDAO;

    public HotelServicioImpl(HotelDAO hotelDAO) {
        this.hotelDAO = Objects.requireNonNull(hotelDAO);
    }

    @Override
    public Optional<Hotel> buscarPorId(int id) {
        return hotelDAO.buscarPorId(id);
    }

    @Override
    public Optional<Hotel> buscarPorRuc(String ruc) {
        return hotelDAO.buscarPorRuc(ruc);
    }

    @Override
    public List<Hotel> listar() {
        return hotelDAO.listar();
    }

    @Override
    public Hotel crear(Hotel hotel) {
        return hotelDAO.crear(Objects.requireNonNull(hotel));
    }

    @Override
    public boolean actualizar(Hotel hotel) {
        return hotelDAO.actualizar(Objects.requireNonNull(hotel));
    }

    @Override
    public boolean eliminar(int id) {
        return hotelDAO.eliminar(id);
    }
}
