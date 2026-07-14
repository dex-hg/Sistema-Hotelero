package hotel.excepcion;

/**
 * Indica que el usuario autenticado no posee el rol requerido para ejecutar
 * una operación de negocio.
 */
public final class AccesoRolException extends RuntimeException {

    public AccesoRolException(String mensaje) {
        super(mensaje);
    }
}
