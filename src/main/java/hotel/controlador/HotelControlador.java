package hotel.controlador;

import hotel.modelo.entidades.Hotel;
import hotel.modelo.servicio.HotelServicio;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class HotelControlador {

    private final HotelServicio servicio;

    public HotelControlador(HotelServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio);
    }

    public Optional<Hotel> buscarPorId(int id) {
        return servicio.buscarPorId(id);
    }

    public Optional<Hotel> buscarPorRuc(String ruc) {
        return servicio.buscarPorRuc(ruc);
    }

    public List<Hotel> listar() {
        return servicio.listar();
    }

    public Hotel crear(Hotel hotel) {
        return servicio.crear(hotel);
    }

    public boolean actualizar(Hotel hotel) {
        return servicio.actualizar(hotel);
    }

    public boolean eliminar(int id) {
        return servicio.eliminar(id);
    }
}
