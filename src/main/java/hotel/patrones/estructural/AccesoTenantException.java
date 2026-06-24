package hotel.patrones.estructural;

public final class AccesoTenantException extends SecurityException {

    private static final long serialVersionUID = 1L;

    public AccesoTenantException() {
        super("La entidad no pertenece al hotel de la sesion activa");
    }
}
