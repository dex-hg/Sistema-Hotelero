package hotel.excepcion;

public final class EntidadNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
