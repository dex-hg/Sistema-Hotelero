package hotel.dao;

public final class DAOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DAOException(String mensaje) {
        super(mensaje);
    }

    public DAOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
