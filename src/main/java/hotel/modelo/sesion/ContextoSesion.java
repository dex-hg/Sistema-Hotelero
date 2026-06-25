package hotel.modelo.sesion;

import hotel.excepcion.SesionNoIniciadaException;
import hotel.modelo.entidades.Usuario;

import java.util.Objects;

public final class ContextoSesion implements ProveedorHotelId {

    private Usuario usuarioActual;

    public void iniciar(Usuario usuario) {
        usuarioActual = Objects.requireNonNull(
                usuario,
                "usuario es obligatorio"
        );
    }

    public void cerrar() {
        usuarioActual = null;
    }

    public boolean estaIniciada() {
        return usuarioActual != null;
    }

    public Usuario getUsuarioActual() {
        if (!estaIniciada()) {
            throw new SesionNoIniciadaException();
        }
        return usuarioActual;
    }

    @Override
    public int getHotelId() {
        return getUsuarioActual().getHotelId();
    }
}
