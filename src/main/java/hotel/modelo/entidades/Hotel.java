package hotel.modelo.entidades;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Hotel {

    private final Integer id;
    private final String nombre;
    private final String ruc;
    private final String direccion;
    private final LocalDateTime creadoEn;

    public Hotel(
            Integer id,
            String nombre,
            String ruc,
            String direccion,
            LocalDateTime creadoEn) {

        this.id = id;
        this.nombre = textoObligatorio(nombre, "nombre");
        this.ruc = textoObligatorio(ruc, "ruc");
        this.direccion = direccion;
        this.creadoEn = Objects.requireNonNull(
                creadoEn,
                "creadoEn es obligatorio"
        );
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

    public String getNombre() {
        return nombre;
    }

    public String getRuc() {
        return ruc;
    }

    public String getDireccion() {
        return direccion;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}
