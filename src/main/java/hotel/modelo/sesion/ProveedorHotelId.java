package hotel.modelo.sesion;

/**
 * Interfaz funcional que provee el identificador del hotel (tenant) activo.
 *
 * APLICA PRINCIPIO SOLID: - ISP (Interface Segregation Principle): En lugar de
 * obligar a los DAOs u otros servicios de infraestructura a depender de una
 * interfaz grande como {@code ContextoSesion} (que expone métodos para
 * iniciar/cerrar sesión y obtener todo el objeto {@code Usuario}), estos
 * componentes de consulta de datos dependen únicamente de esta pequeña interfaz
 * de un solo método. Esto evita acoplar el acceso a datos con la gestión y
 * administración de sesiones del usuario.
 */
@FunctionalInterface
public interface ProveedorHotelId {

    int getHotelId();
}
