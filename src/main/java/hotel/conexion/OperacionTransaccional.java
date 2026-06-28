package hotel.conexion;

@FunctionalInterface
public interface OperacionTransaccional<T> {

    T ejecutar();
}
