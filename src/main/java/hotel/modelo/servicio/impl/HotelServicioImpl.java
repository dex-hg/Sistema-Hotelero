package hotel.modelo.servicio.impl;

import hotel.dao.HotelDAO;

import hotel.modelo.entidades.Hotel;
import hotel.modelo.servicio.HotelServicio;
import hotel.modelo.sesion.ProveedorHotelId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class HotelServicioImpl implements HotelServicio {

    private final HotelDAO hotelDAO;
    private final ProveedorHotelId proveedorHotelId;

    public HotelServicioImpl(
            HotelDAO hotelDAO,
            ProveedorHotelId proveedorHotelId
    ) {
        this.hotelDAO = Objects.requireNonNull(hotelDAO);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
    }

    @Override
    public Optional<Hotel> buscarPorId(int id) {
        if (id != proveedorHotelId.getHotelId()) {
            return Optional.empty();
        }
        return hotelDAO.buscarPorId(id);
    }

    @Override
    public Optional<Hotel> buscarPorRuc(String ruc) {
        int hotelId = proveedorHotelId.getHotelId();
        return hotelDAO.buscarPorRuc(ruc)
                .filter(hotel -> hotel.getId() == hotelId);
    }

    @Override
    public List<Hotel> listar() {
        return buscarPorId(proveedorHotelId.getHotelId())
                .map(List::of)
                .orElseGet(List::of);
    }
}
