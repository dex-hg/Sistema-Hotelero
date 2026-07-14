package hotel.modelo.servicio.impl;

import hotel.dao.HabitacionDAO;

import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.constantes.TipoHabitacion;
import hotel.modelo.seguridad.AutorizadorAcceso;
import hotel.modelo.sesion.ProveedorHotelId;
import hotel.modelo.servicio.HabitacionServicio;

import hotel.patrones.creacional.HabitacionBuilder;

import hotel.excepcion.EntidadNoEncontradaException;
import hotel.excepcion.ReglaNegocioException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Servicio de negocio de habitaciones.
 *
 * APLICA PRINCIPIO SOLID: - SRP: valida reglas de habitaciones y coordina el
 * DAO, sin manejar SQL ni UI. - DIP: depende de la abstraccion
 * {@link HabitacionDAO}, no de JDBC directo.
 */
public final class HabitacionServicioImpl implements HabitacionServicio {

    private final HabitacionDAO habitacionDAO;
    private final ProveedorHotelId proveedorHotelId;
    private final AutorizadorAcceso autorizadorAcceso;

    public HabitacionServicioImpl(
            HabitacionDAO habitacionDAO,
            ProveedorHotelId proveedorHotelId,
            AutorizadorAcceso autorizadorAcceso
    ) {
        this.habitacionDAO = Objects.requireNonNull(habitacionDAO);
        this.proveedorHotelId = Objects.requireNonNull(proveedorHotelId);
        this.autorizadorAcceso = Objects.requireNonNull(autorizadorAcceso);
    }

    @Override
    public Habitacion buscarPorId(int id) {
        return habitacionDAO.buscarPorId(id).orElseThrow(
                () -> new EntidadNoEncontradaException(
                        "No existe la habitacion " + id
                )
        );
    }

    @Override
    public Optional<Habitacion> buscarPorNumero(String numero) {
        return habitacionDAO.buscarPorNumero(numero);
    }

    @Override
    public List<Habitacion> listar() {
        return habitacionDAO.listar();
    }

    @Override
    public Habitacion crear(
            String numero,
            TipoHabitacion tipo,
            BigDecimal precioPorNoche,
            int cantidadCamas,
            boolean banoPrivado,
            boolean tv
    ) {
        autorizadorAcceso.exigirAdministrador();
        if (habitacionDAO.buscarPorNumero(numero).isPresent()) {
            throw new ReglaNegocioException(
                    "Ya existe una habitacion con el numero " + numero
            );
        }

        HabitacionBuilder builder = new HabitacionBuilder()
                .paraHotel(proveedorHotelId.getHotelId())
                .conNumero(numero)
                .deTipo(tipo)
                .conPrecioPorNoche(precioPorNoche)
                .conCantidadCamas(cantidadCamas);

        if (banoPrivado) {
            builder.conBanoPrivado();
        }
        if (tv) {
            builder.conTv();
        }

        return habitacionDAO.crear(builder.construir());
    }

    @Override
    public Habitacion actualizar(
            int id,
            String numero,
            TipoHabitacion tipo,
            BigDecimal precioPorNoche,
            int cantidadCamas,
            boolean banoPrivado,
            boolean tv
    ) {
        autorizadorAcceso.exigirAdministrador();
        Habitacion actual = buscarPorId(id);
        Optional<Habitacion> duplicada = habitacionDAO.buscarPorNumero(numero);
        if (duplicada.isPresent() && !duplicada.orElseThrow().getId().equals(id)) {
            throw new ReglaNegocioException(
                    "Ya existe una habitacion con el numero " + numero
            );
        }

        HabitacionBuilder builder = new HabitacionBuilder()
                .conId(actual.getId())
                .paraHotel(actual.getHotelId())
                .conNumero(numero)
                .deTipo(tipo)
                .conPrecioPorNoche(precioPorNoche)
                .conCantidadCamas(cantidadCamas)
                .conEstado(actual.getEstado());
        if (banoPrivado) {
            builder.conBanoPrivado();
        }
        if (tv) {
            builder.conTv();
        }

        Habitacion actualizada = builder.construir();
        if (!habitacionDAO.actualizar(actualizada)) {
            throw new EntidadNoEncontradaException(
                    "La habitacion dejó de existir durante la actualización"
            );
        }
        return actualizada;
    }

    @Override
    public boolean eliminar(int id) {
        autorizadorAcceso.exigirAdministrador();
        return habitacionDAO.eliminar(id);
    }

    @Override
    public Habitacion ocupar(int id) {
        return cambiarEstado(id, Habitacion::ocupar);
    }

    @Override
    public Habitacion iniciarLimpieza(int id) {
        return cambiarEstado(id, Habitacion::iniciarLimpieza);
    }

    @Override
    public Habitacion habilitar(int id) {
        return cambiarEstado(id, Habitacion::habilitar);
    }

    @Override
    public Habitacion enviarAMantenimiento(int id) {
        autorizadorAcceso.exigirAdministrador();
        return cambiarEstado(id, Habitacion::enviarAMantenimiento);
    }

    /**
     * Aplica una transicion del patron State y persiste el estado resultante.
     *
     * PATRON DE DISENO: - State: la validez de la transicion vive en la
     * entidad/estado concreto; el servicio solo coordina la busqueda y
     * guardado.
     */
    private Habitacion cambiarEstado(int id, Consumer<Habitacion> transicion) {
        Habitacion habitacion = buscarPorId(id);
        transicion.accept(habitacion);

        if (!habitacionDAO.actualizar(habitacion)) {
            throw new EntidadNoEncontradaException(
                    "La habitacion dejo de existir "
                    + "durante la actualizacion"
            );
        }
        return habitacion;
    }
}
