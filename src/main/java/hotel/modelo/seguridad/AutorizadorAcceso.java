package hotel.modelo.seguridad;

/**
 * Contrato mínimo para aplicar permisos de negocio sin acoplar los servicios
 * al mecanismo concreto que mantiene la sesión.
 */
@FunctionalInterface
public interface AutorizadorAcceso {

    void exigirAdministrador();
}
