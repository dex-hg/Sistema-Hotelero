package hotel.modelo.servicio;

import hotel.modelo.entidades.Usuario;

public interface AutenticacionServicio {

    Usuario iniciarSesion(String rucHotel, String username, String password);

    Usuario iniciarSesion(int hotelId, String username, String password);

    void cerrarSesion();

    boolean sesionIniciada();

    Usuario usuarioActual();
}
