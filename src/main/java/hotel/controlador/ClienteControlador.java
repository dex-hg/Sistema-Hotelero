package hotel.controlador;

import hotel.modelo.entidades.Cliente;
import hotel.modelo.servicio.ClienteServicio;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ClienteControlador {

    private final ClienteServicio servicio;

    public ClienteControlador(ClienteServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio);
    }

    public List<Cliente> listar() {
        return servicio.listar();
    }

    public Cliente buscarPorId(int id) {
        return servicio.buscarPorId(id);
    }

    public Optional<Cliente> buscarPorDocumento(String documento) {
        return servicio.buscarPorDocumento(documento);
    }

    public Cliente crear(
            String nombre,
            String documento,
            String telefono
    ) {
        return servicio.crear(
                nombre,
                documento,
                telefono
        );
    }

    public Cliente guardarOActualizarPorDocumento(
            String nombre,
            String documento,
            String telefono
    ) {
        return servicio.guardarOActualizarPorDocumento(
                nombre,
                documento,
                telefono
        );
    }

    public boolean actualizar(Cliente cliente) {
        return servicio.actualizar(cliente);
    }

    public boolean eliminar(int id) {
        return servicio.eliminar(id);
    }
}
