package hotel.modelo.entidades;

import hotel.modelo.entidades.constantes.RolUsuario;
import java.util.Objects;

public final class Usuario {

    private final Integer id;
    private final int hotelId;
    private final String username;
    private final String password;
    private final RolUsuario rol;

    public Usuario(
            Integer id,
            int hotelId,
            String username,
            String password,
            RolUsuario rol
    ) {

        if (hotelId <= 0) {
            throw new IllegalArgumentException("hotelId debe ser positivo");
        }

        this.id = id;
        this.hotelId = hotelId;
        this.username = textoObligatorio(username, "username");
        this.password = textoObligatorio(password, "password");
        this.rol = Objects.requireNonNull(rol, "rol es obligatorio");
    }

    private static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }

        return valor.trim();
    }

    public Integer getId() {
        return id;
    }

    public int getHotelId() {
        return hotelId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public RolUsuario getRol() {
        return rol;
    }
}
