package hotel.configuracion;

import hotel.conexion.ConexionBD;
import hotel.conexion.EjecutorTransaccional;
import hotel.conexion.ProveedorConexion;
import hotel.conexion.ProveedorConexionTransaccional;

import hotel.controlador.*;

import hotel.dao.*;
import hotel.dao.jdbc.*;

import hotel.modelo.servicio.*;
import hotel.modelo.servicio.impl.*;
import hotel.modelo.sesion.ContextoSesion;

import hotel.patrones.estructural.*;

import java.util.Objects;

/**
 * Raiz de composicion de la aplicacion.
 *
 * APLICA PRINCIPIO SOLID: - DIP: arma el grafo de objetos conectando interfaces
 * de DAO y servicios con sus implementaciones concretas en un unico lugar. -
 * SRP: centraliza la configuracion de dependencias y evita que la UI o los
 * controladores conozcan detalles de JDBC, Proxy o transacciones.
 */
public final class ComposicionAplicacion {

    private final ProveedorConexion proveedorConexion;
    private final EjecutorTransaccional ejecutorTransaccional;
    private final ContextoSesion contextoSesion;

    private final HotelDAO hotelDAO;
    private final AutenticacionUsuarioDAO autenticacionUsuarioDAO;
    private final UsuarioDAO usuarioDAO;
    private final HabitacionDAO habitacionDAO;
    private final ClienteDAO clienteDAO;
    private final ReservaDAO reservaDAO;

    private final AutenticacionServicio autenticacionServicio;
    private final HotelServicio hotelServicio;
    private final HabitacionServicio habitacionServicio;
    private final ClienteServicio clienteServicio;
    private final ReservaServicio reservaServicio;

    private final AutenticacionControlador autenticacionControlador;
    private final HotelControlador hotelControlador;
    private final HabitacionControlador habitacionControlador;
    private final ClienteControlador clienteControlador;
    private final ReservaControlador reservaControlador;

    public ComposicionAplicacion() {
        this(
                new ProveedorConexionTransaccional(new ConexionBD()),
                new ContextoSesion()
        );
    }

    public ComposicionAplicacion(
            ProveedorConexion proveedorConexion,
            ContextoSesion contextoSesion
    ) {
        this.proveedorConexion = prepararProveedorConexion(
                Objects.requireNonNull(proveedorConexion)
        );
        this.ejecutorTransaccional
                = (EjecutorTransaccional) this.proveedorConexion;

        this.contextoSesion = Objects.requireNonNull(contextoSesion);

        this.hotelDAO = new HotelDAOJdbc(
                this.proveedorConexion
        );

        this.autenticacionUsuarioDAO = new AutenticacionUsuarioDAOJdbc(
                this.proveedorConexion
        );

        this.usuarioDAO = new UsuarioDAOProxy(
                new UsuarioDAOJdbc(
                        this.proveedorConexion,
                        this.contextoSesion
                ),
                this.contextoSesion
        );

        this.habitacionDAO = new HabitacionDAOProxy(
                new HabitacionDAOJdbc(
                        this.proveedorConexion,
                        this.contextoSesion
                ),
                this.contextoSesion
        );

        this.clienteDAO = new ClienteDAOProxy(
                new ClienteDAOJdbc(
                        this.proveedorConexion,
                        this.contextoSesion
                ),
                this.contextoSesion
        );

        this.reservaDAO = new ReservaDAOProxy(
                new ReservaDAOJdbc(
                        this.proveedorConexion,
                        this.contextoSesion
                ),
                this.contextoSesion
        );

        this.autenticacionServicio = new AutenticacionServicioImpl(
                this.autenticacionUsuarioDAO,
                this.hotelDAO,
                this.contextoSesion
        );

        this.hotelServicio = new HotelServicioImpl(
                this.hotelDAO
        );

        this.habitacionServicio = new HabitacionServicioImpl(
                this.habitacionDAO,
                this.contextoSesion
        );

        this.clienteServicio = new ClienteServicioImpl(
                this.clienteDAO,
                this.contextoSesion
        );

        this.reservaServicio = new ReservaServicioImpl(
                this.reservaDAO,
                this.habitacionDAO,
                this.clienteDAO,
                this.contextoSesion,
                this.ejecutorTransaccional
        );

        this.autenticacionControlador = new AutenticacionControlador(
                this.autenticacionServicio
        );

        this.hotelControlador = new HotelControlador(
                this.hotelServicio
        );

        this.habitacionControlador = new HabitacionControlador(
                this.habitacionServicio
        );

        this.clienteControlador = new ClienteControlador(
                this.clienteServicio
        );

        this.reservaControlador = new ReservaControlador(
                this.reservaServicio
        );
    }

    public ContextoSesion contextoSesion() {
        return contextoSesion;
    }

    public EjecutorTransaccional ejecutorTransaccional() {
        return ejecutorTransaccional;
    }

    public HotelDAO hotelDAO() {
        return hotelDAO;
    }

    public UsuarioDAO usuarioDAO() {
        return usuarioDAO;
    }

    public HabitacionDAO habitacionDAO() {
        return habitacionDAO;
    }

    public ClienteDAO clienteDAO() {
        return clienteDAO;
    }

    public ReservaDAO reservaDAO() {
        return reservaDAO;
    }

    public AutenticacionControlador autenticacionControlador() {
        return autenticacionControlador;
    }

    public HotelControlador hotelControlador() {
        return hotelControlador;
    }

    public HabitacionControlador habitacionControlador() {
        return habitacionControlador;
    }

    public ClienteControlador clienteControlador() {
        return clienteControlador;
    }

    public ReservaControlador reservaControlador() {
        return reservaControlador;
    }

    private ProveedorConexion prepararProveedorConexion(
            ProveedorConexion proveedorConexion
    ) {
        if (proveedorConexion instanceof EjecutorTransaccional transaccional) {
            return (ProveedorConexion) transaccional;
        }

        return new ProveedorConexionTransaccional(
                proveedorConexion
        );
    }
}
