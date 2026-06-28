package hotel.controlador;

import hotel.modelo.entidades.Usuario;
import hotel.modelo.servicio.AutenticacionServicio;

import java.util.Objects;

public final class AutenticacionControlador {

    private final AutenticacionServicio servicio;

    public AutenticacionControlador(AutenticacionServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio);
    }

    public Usuario iniciarSesion(
            String rucHotel,
            String username,
            String password
    ) {
        return servicio.iniciarSesion(rucHotel, username, password);
    }

    public Usuario iniciarSesion(
            int hotelId,
            String username,
            String password
    ) {
        return servicio.iniciarSesion(hotelId, username, password);
    }

    public void cerrarSesion() {
        servicio.cerrarSesion();
    }

    public boolean sesionIniciada() {
        return servicio.sesionIniciada();
    }

    public Usuario usuarioActual() {
        return servicio.usuarioActual();
    }
}
