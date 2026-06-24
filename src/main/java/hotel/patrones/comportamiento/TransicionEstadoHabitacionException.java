package hotel.patrones.comportamiento;

public final class TransicionEstadoHabitacionException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public TransicionEstadoHabitacionException(String mensaje) {
        super(mensaje);
    }
}
