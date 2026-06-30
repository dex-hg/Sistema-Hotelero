package hotel.modelo.entidades;

public final class Cliente {

    private final Integer id;
    private final int hotelId;
    private final String nombreCompleto;
    private final String documentoIdentidad;
    private final String telefono;

    public Cliente(
            Integer id,
            int hotelId,
            String nombreCompleto,
            String documentoIdentidad,
            String telefono) {

        if (hotelId <= 0) {
            throw new IllegalArgumentException(
                    "hotelId debe ser positivo"
            );
        }

        this.id = id;
        this.hotelId = hotelId;
        this.nombreCompleto = textoObligatorio(
                nombreCompleto,
                "nombreCompleto"
        );
        this.documentoIdentidad = textoObligatorio(
                documentoIdentidad,
                "documentoIdentidad"
        );
        this.telefono = telefono;
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

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public String getTelefono() {
        return telefono;
    }
}
