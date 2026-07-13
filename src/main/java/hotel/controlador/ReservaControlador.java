package hotel.controlador;

import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.Cliente;
import hotel.modelo.servicio.DatosHuespedRecepcion;
import hotel.modelo.servicio.ReservaServicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Objects;
import java.util.Map;

public final class ReservaControlador {

    private final ReservaServicio servicio;

    public ReservaControlador(ReservaServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio);
    }

    public List<Reserva> listar() {
        return servicio.listar();
    }

    public int finalizarVencidas() {
        return servicio.finalizarVencidas(LocalDateTime.now());
    }

    public Reserva buscarPorId(int id) {
        return servicio.buscarPorId(id);
    }

    public List<Cliente> listarHuespedes(int reservaId) {
        return servicio.listarHuespedes(reservaId);
    }

    public Map<Integer, List<Cliente>> listarHuespedesPorReserva() {
        return servicio.listarHuespedesPorReserva();
    }

    public BigDecimal calcularTotalHospedaje(
            BigDecimal precioPorNoche,
            LocalDateTime ingreso,
            LocalDateTime salida
    ) {
        return servicio.calcularTotalHospedaje(
                precioPorNoche,
                ingreso,
                salida
        );
    }

    public Reserva crear(
            int habitacionId,
            int clienteId,
            LocalDateTime ingreso,
            LocalDateTime salida
    ) {
        return servicio.crear(
                habitacionId,
                clienteId,
                ingreso,
                salida
        );
    }

    public Reserva registrarRecepcion(
            String nombreCompleto,
            String documentoIdentidad,
            String telefono,
            int habitacionId,
            LocalDateTime ingreso,
            LocalDateTime salida
    ) {
        return servicio.registrarRecepcion(
                nombreCompleto,
                documentoIdentidad,
                telefono,
                habitacionId,
                ingreso,
                salida
        );
    }

    public Reserva registrarRecepcion(
            String nombreCompleto,
            String documentoIdentidad,
            String telefono,
            int habitacionId,
            LocalDateTime ingreso,
            LocalDateTime salida,
            List<DatosHuespedRecepcion> huespedesAdicionales
    ) {
        return servicio.registrarRecepcion(
                nombreCompleto,
                documentoIdentidad,
                telefono,
                habitacionId,
                ingreso,
                salida,
                huespedesAdicionales
        );
    }

    public Reserva registrarPago(int id, BigDecimal monto) {
        return servicio.registrarPago(id, monto);
    }

    public Reserva registrarCheckIn(int id) {
        return servicio.registrarCheckIn(id);
    }

    public Reserva registrarCheckOut(int id) {
        return servicio.registrarCheckOut(id);
    }

    public Reserva cancelar(int id) {
        return servicio.cancelar(id);
    }

    public Reserva cancelarRecepcion(int id) {
        return servicio.cancelarRecepcion(id);
    }

    public Reserva finalizar(int id) {
        return servicio.finalizar(id);
    }

    public boolean eliminar(int id) {
        return servicio.eliminar(id);
    }
}
