package hotel.modelo.servicio.impl;

import hotel.dao.ClienteDAO;

import hotel.modelo.entidades.Cliente;
import hotel.modelo.sesion.ProveedorHotelId;
import hotel.modelo.servicio.ClienteServicio;

import hotel.excepcion.EntidadNoEncontradaException;
import hotel.excepcion.ReglaNegocioException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ClienteServicioImpl implements ClienteServicio {

    private final ClienteDAO clienteDAO;
    private final ProveedorHotelId proveedorHotelId;

    public ClienteServicioImpl(
            ClienteDAO clienteDAO,
            ProveedorHotelId proveedorHotelId
    ) {
        this.clienteDAO = Objects.requireNonNull(clienteDAO);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
    }

    @Override
    public Cliente buscarPorId(int id) {
        return clienteDAO.buscarPorId(id).orElseThrow(
                () -> new EntidadNoEncontradaException(
                        "No existe el cliente " + id
                )
        );
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documentoIdentidad) {
        return clienteDAO.buscarPorDocumento(documentoIdentidad);
    }

    @Override
    public List<Cliente> listar() {
        return clienteDAO.listar();
    }

    @Override
    public Cliente crear(
            String nombreCompleto,
            String documentoIdentidad,
            String telefono
    ) {
        if (clienteDAO.buscarPorDocumento(documentoIdentidad).isPresent()) {
            throw new ReglaNegocioException(
                    "Ya existe un cliente con el documento "
                    + documentoIdentidad
            );
        }

        return clienteDAO.crear(
                new Cliente(
                        null,
                        proveedorHotelId.getHotelId(),
                        nombreCompleto,
                        documentoIdentidad,
                        telefono
                )
        );
    }

    @Override
    public Cliente guardarOActualizarPorDocumento(
            String nombreCompleto,
            String documentoIdentidad,
            String telefono
    ) {
        Optional<Cliente> existente = clienteDAO.buscarPorDocumento(documentoIdentidad);
        if (existente.isEmpty()) {
            return crear(nombreCompleto, documentoIdentidad, telefono);
        }

        Cliente cliente = existente.orElseThrow();
        Cliente actualizado = new Cliente(
                cliente.getId(),
                proveedorHotelId.getHotelId(),
                nombreCompleto,
                documentoIdentidad,
                telefono
        );

        if (!clienteDAO.actualizar(actualizado)) {
            throw new EntidadNoEncontradaException(
                    "El cliente dejo de existir durante la actualizacion"
            );
        }

        return actualizado;
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        Objects.requireNonNull(cliente, "cliente es obligatorio");
        return clienteDAO.actualizar(cliente);
    }

    @Override
    public boolean eliminar(int id) {
        return clienteDAO.eliminar(id);
    }
}
