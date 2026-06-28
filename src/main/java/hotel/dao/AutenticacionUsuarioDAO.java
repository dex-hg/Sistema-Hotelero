package hotel.dao;

import hotel.modelo.entidades.Usuario;

import java.util.Optional;

public interface AutenticacionUsuarioDAO {

    Optional<Usuario> buscarPorHotelYUsername(int hotelId, String username);
}
