package hotel.configuracion;

import hotel.conexion.ConexionBD;
import hotel.conexion.EjecutorTransaccional;
import hotel.conexion.ProveedorConexion;
import hotel.conexion.ProveedorConexionTransaccional;

import hotel.controlador.AutenticacionControlador;
import hotel.controlador.ClienteControlador;
import hotel.controlador.EstadisticasControlador;
import hotel.controlador.HabitacionControlador;
import hotel.controlador.HotelControlador;
import hotel.controlador.ReservaControlador;

import hotel.dao.AutenticacionUsuarioDAO;
import hotel.dao.ClienteDAO;
import hotel.dao.HabitacionDAO;
import hotel.dao.HotelDAO;
import hotel.dao.ReservaDAO;
import hotel.dao.UsuarioDAO;
import hotel.dao.jdbc.AutenticacionUsuarioDAOJdbc;
import hotel.dao.jdbc.ClienteDAOJdbc;
import hotel.dao.jdbc.HabitacionDAOJdbc;
import hotel.dao.jdbc.HotelDAOJdbc;
import hotel.dao.jdbc.ReservaDAOJdbc;
import hotel.dao.jdbc.UsuarioDAOJdbc;

import hotel.modelo.servicio.AutenticacionServicio;
import hotel.modelo.servicio.ClienteServicio;
import hotel.modelo.servicio.EstadisticasServicio;
import hotel.modelo.servicio.HabitacionServicio;
import hotel.modelo.servicio.HotelServicio;
import hotel.modelo.servicio.ReservaServicio;
import hotel.modelo.servicio.impl.AutenticacionServicioImpl;
import hotel.modelo.servicio.impl.ClienteServicioImpl;
import hotel.modelo.servicio.impl.EstadisticasServicioImpl;
import hotel.modelo.servicio.impl.HabitacionServicioImpl;
import hotel.modelo.servicio.impl.HotelServicioImpl;
import hotel.modelo.servicio.impl.ReservaServicioImpl;
import hotel.modelo.seguridad.AutorizadorAcceso;
import hotel.modelo.seguridad.AutorizadorSesion;
import hotel.modelo.sesion.ContextoSesion;

import hotel.patrones.estructural.ClienteDAOProxy;
import hotel.patrones.estructural.HabitacionDAOProxy;
import hotel.patrones.estructural.ReservaDAOProxy;
import hotel.patrones.estructural.UsuarioDAOProxy;

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
    private final EstadisticasServicio estadisticasServicio;

    private final AutenticacionControlador autenticacionControlador;
    private final HotelControlador hotelControlador;
    private final HabitacionControlador habitacionControlador;
    private final ClienteControlador clienteControlador;
    private final ReservaControlador reservaControlador;
    private final EstadisticasControlador estadisticasControlador;

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
        AutorizadorAcceso autorizadorAcceso = new AutorizadorSesion(
                this.contextoSesion
        );

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
                this.contextoSesion,
                autorizadorAcceso
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
                this.hotelDAO,
                this.contextoSesion
        );

        this.habitacionServicio = new HabitacionServicioImpl(
                this.habitacionDAO,
                this.contextoSesion,
                autorizadorAcceso
        );

        this.clienteServicio = new ClienteServicioImpl(
                this.clienteDAO,
                this.contextoSesion,
                autorizadorAcceso
        );

        this.reservaServicio = new ReservaServicioImpl(
                this.reservaDAO,
                this.habitacionDAO,
                this.clienteDAO,
                this.contextoSesion,
                this.ejecutorTransaccional,
                autorizadorAcceso
        );

        this.estadisticasServicio = new EstadisticasServicioImpl(
                this.habitacionDAO,
                this.clienteDAO,
                this.reservaDAO
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

        this.estadisticasControlador = new EstadisticasControlador(
                this.estadisticasServicio
        );
    }

    public ContextoSesion contextoSesion() {
        return contextoSesion;
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

    public EstadisticasControlador estadisticasControlador() {
        return estadisticasControlador;
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
