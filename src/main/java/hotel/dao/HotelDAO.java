package hotel.dao;

import hotel.modelo.entidades.Hotel;

import java.util.List;
import java.util.Optional;

public interface HotelDAO {

    Optional<Hotel> buscarPorId(int id);

    Optional<Hotel> buscarPorRuc(String ruc);

    List<Hotel> listar();

    Hotel crear(Hotel hotel);

    boolean actualizar(Hotel hotel);

    boolean eliminar(int id);
}
