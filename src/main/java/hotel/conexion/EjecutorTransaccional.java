package hotel.conexion;

@FunctionalInterface
public interface EjecutorTransaccional {

    <T> T ejecutar(OperacionTransaccional<T> operacion);
}
