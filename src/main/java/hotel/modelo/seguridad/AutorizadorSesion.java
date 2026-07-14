package hotel.modelo.seguridad;

import hotel.excepcion.AccesoRolException;
import hotel.modelo.entidades.constantes.RolUsuario;
import hotel.modelo.sesion.ContextoSesion;

import java.util.Objects;

/**
 * Autoriza operaciones a partir del usuario que mantiene la sesión actual.
 */
public final class AutorizadorSesion implements AutorizadorAcceso {

    private final ContextoSesion contextoSesion;

    public AutorizadorSesion(ContextoSesion contextoSesion) {
        this.contextoSesion = Objects.requireNonNull(contextoSesion);
    }

    @Override
    public void exigirAdministrador() {
        if (contextoSesion.getUsuarioActual().getRol()
                != RolUsuario.ADMINISTRADOR) {
            throw new AccesoRolException(
                    "Esta operación requiere el rol de administrador"
            );
        }
    }
}
