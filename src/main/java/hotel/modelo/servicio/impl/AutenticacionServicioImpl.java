package hotel.modelo.servicio.impl;

import hotel.modelo.entidades.Usuario;
import hotel.modelo.sesion.ContextoSesion;
import hotel.modelo.servicio.AutenticacionServicio;

import hotel.dao.AutenticacionUsuarioDAO;
import hotel.dao.HotelDAO;

import hotel.excepcion.ReglaNegocioException;

import java.util.Objects;

/**
 * Servicio de autenticacion y ciclo de sesion.
 *
 * APLICA PRINCIPIO SOLID: - SRP: concentra validacion de credenciales e
 * inicio/cierre de sesion. - DIP: usa {@link AutenticacionUsuarioDAO} y
 * {@link HotelDAO} como abstracciones de lectura, sin acoplarse a JDBC.
 */
public final class AutenticacionServicioImpl implements AutenticacionServicio {

    private final AutenticacionUsuarioDAO autenticacionUsuarioDAO;
    private final HotelDAO hotelDAO;
    private final ContextoSesion contextoSesion;

    public AutenticacionServicioImpl(
            AutenticacionUsuarioDAO autenticacionUsuarioDAO,
            HotelDAO hotelDAO,
            ContextoSesion contextoSesion
    ) {
        this.autenticacionUsuarioDAO = Objects.requireNonNull(
                autenticacionUsuarioDAO
        );

        this.hotelDAO = Objects.requireNonNull(
                hotelDAO
        );

        this.contextoSesion = Objects.requireNonNull(
                contextoSesion
        );
    }

    @Override
    public Usuario iniciarSesion(
            String rucHotel,
            String username,
            String password
    ) {
        if (rucHotel == null || rucHotel.isBlank()) {
            throw new ReglaNegocioException(
                    "Ingrese el RUC del hotel"
            );
        }

        int hotelId = hotelDAO.buscarPorRuc(rucHotel.trim())
                .orElseThrow(() -> new ReglaNegocioException(
                "Credenciales invalidas"
        ))
                .getId();

        return iniciarSesion(hotelId, username, password);
    }

    @Override
    public Usuario iniciarSesion(
            int hotelId,
            String username,
            String password
    ) {
        if (username == null || username.isBlank()) {
            throw new ReglaNegocioException("Ingrese el usuario");
        }
        if (password == null || password.isBlank()) {
            throw new ReglaNegocioException("Ingrese la contrasena");
        }

        Usuario usuario = autenticacionUsuarioDAO
                .buscarPorHotelYUsername(hotelId, username.trim())
                .orElseThrow(() -> new ReglaNegocioException(
                "Credenciales invalidas"
        )
                );

        if (!usuario.getPassword().equals(password)) {
            throw new ReglaNegocioException(
                    "Credenciales invalidas"
            );
        }

        contextoSesion.iniciar(usuario);
        return usuario;
    }

    @Override
    public void cerrarSesion() {
        contextoSesion.cerrar();
    }

    @Override
    public boolean sesionIniciada() {
        return contextoSesion.estaIniciada();
    }

    @Override
    public Usuario usuarioActual() {
        return contextoSesion.getUsuarioActual();
    }
}
