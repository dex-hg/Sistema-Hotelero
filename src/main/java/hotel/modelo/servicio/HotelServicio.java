package hotel.modelo.servicio;

import hotel.modelo.entidades.Hotel;

import java.util.List;
import java.util.Optional;

public interface HotelServicio {

    Optional<Hotel> buscarPorId(int id);

    Optional<Hotel> buscarPorRuc(String ruc);

    List<Hotel> listar();

}
