package hotel.modelo.servicio;

import hotel.modelo.entidades.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteServicio {

    Cliente buscarPorId(int id);

    Optional<Cliente> buscarPorDocumento(String documentoIdentidad);

    List<Cliente> listar();

    Cliente crear(
            String nombreCompleto,
            String documentoIdentidad,
            String telefono
    );

    Cliente guardarOActualizarPorDocumento(
            String nombreCompleto,
            String documentoIdentidad,
            String telefono
    );

    Cliente actualizar(
            int id,
            String nombreCompleto,
            String documentoIdentidad,
            String telefono
    );

    boolean eliminar(int id);
}
