package hotel.excepcion;

public final class SesionNoIniciadaException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public SesionNoIniciadaException() {
        super("No existe una sesion de usuario activa");
    }
}
