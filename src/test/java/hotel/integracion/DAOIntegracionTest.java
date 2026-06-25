package hotel.integracion;

import hotel.conexion.ConexionBD;
import hotel.conexion.ConexionConfig;
import hotel.conexion.ProveedorConexion;
import hotel.controlador.ClienteControlador;
import hotel.controlador.HabitacionControlador;
import hotel.controlador.ReservaControlador;
import hotel.dao.ClienteDAO;
import hotel.excepcion.DAOException;
import hotel.dao.HabitacionDAO;
import hotel.dao.HotelDAO;
import hotel.dao.ReservaDAO;
import hotel.dao.UsuarioDAO;
import hotel.dao.jdbc.ClienteDAOJdbc;
import hotel.dao.jdbc.HabitacionDAOJdbc;
import hotel.dao.jdbc.HotelDAOJdbc;
import hotel.dao.jdbc.ReservaDAOJdbc;
import hotel.dao.jdbc.UsuarioDAOJdbc;
import hotel.modelo.entidades.Cliente;
import hotel.modelo.entidades.Habitacion;
import hotel.modelo.entidades.Hotel;
import hotel.modelo.entidades.Reserva;
import hotel.modelo.entidades.Usuario;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.EstadoReserva;
import hotel.modelo.entidades.constantes.RolUsuario;
import hotel.modelo.entidades.constantes.TipoHabitacion;
import hotel.modelo.sesion.ContextoSesion;
import hotel.excepcion.SesionNoIniciadaException;
import hotel.patrones.creacional.HabitacionBuilder;
import hotel.excepcion.AccesoTenantException;
import hotel.patrones.estructural.ClienteDAOProxy;
import hotel.patrones.estructural.HabitacionDAOProxy;
import hotel.patrones.estructural.ReservaDAOProxy;
import hotel.patrones.estructural.UsuarioDAOProxy;
import hotel.excepcion.ReglaNegocioException;
import hotel.modelo.servicio.impl.ClienteServicioImpl;
import hotel.modelo.servicio.impl.HabitacionServicioImpl;
import hotel.modelo.servicio.impl.ReservaServicioImpl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

/**
 * Prueba de integracion ejecutable sin framework externo.
 *
 * Usa un esquema temporal y lo elimina al finalizar, por lo que no modifica
 * los datos existentes de sistema_hotel.
 */
public final class DAOIntegracionTest {

    private DAOIntegracionTest() {
    }

    public static void main(String[] args) throws Exception {
        new DAOIntegracionTest().validarDaoJdbcYProxyConPostgreSQL();
    }

    @Test
    void validarDaoJdbcYProxyConPostgreSQL() throws Exception {
        String esquema = "hostelflow_it_" + Long.toUnsignedString(System.nanoTime());
        ConexionBD conexionBase = new ConexionBD();

        try {
            crearEsquema(conexionBase, esquema);
            ProveedorConexion conexiones = () -> conexionEnEsquema(conexionBase, esquema);
            ejecutarPruebas(conexiones);
            System.out.println("OK: integracion DAO JDBC + Proxy + PostgreSQL");
        } finally {
            eliminarEsquema(conexionBase, esquema);
        }
    }

    private static void ejecutarPruebas(ProveedorConexion conexiones) {
        HotelDAO hoteles = new HotelDAOJdbc(conexiones);
        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        Hotel hotelUno = hoteles.crear(new Hotel(null, "Hotel Uno", "10000000001", "Lima", ahora));
        Hotel hotelDos = hoteles.crear(new Hotel(null, "Hotel Dos", "10000000002", "Cusco", ahora));

        verificar(hotelUno.getId() != null, "HotelDAO debe devolver el id generado");
        verificar(hoteles.buscarPorRuc("10000000001").isPresent(), "Debe buscar hotel por RUC");
        verificar(hoteles.listar().size() == 2, "Debe listar ambos hoteles");

        ContextoSesion sesion = new ContextoSesion();
        UsuarioDAO usuarios = new UsuarioDAOProxy(new UsuarioDAOJdbc(conexiones, sesion), sesion);
        HabitacionDAO habitaciones = new HabitacionDAOProxy(new HabitacionDAOJdbc(conexiones, sesion), sesion);
        ClienteDAO clientes = new ClienteDAOProxy(new ClienteDAOJdbc(conexiones, sesion), sesion);
        ReservaDAO reservas = new ReservaDAOProxy(new ReservaDAOJdbc(conexiones, sesion), sesion);

        esperarExcepcion(SesionNoIniciadaException.class, habitaciones::listar,
                "El Proxy debe exigir una sesion activa");

        Usuario principalUno = new Usuario(null, hotelUno.getId(), "admin1", "texto-plano",
                RolUsuario.ADMINISTRADOR);
        sesion.iniciar(principalUno);
        Usuario usuarioUno = usuarios.crear(principalUno);
        verificar(usuarioUno.getId() != null, "UsuarioDAO debe devolver el id generado");

        HabitacionControlador habitacionControlador = new HabitacionControlador(
                new HabitacionServicioImpl(habitaciones, sesion));
        ClienteControlador clienteControlador = new ClienteControlador(
                new ClienteServicioImpl(clientes, sesion));
        ReservaControlador reservaControlador = new ReservaControlador(
                new ReservaServicioImpl(reservas, habitaciones, clientes, sesion));

        esperarExcepcion(AccesoTenantException.class,
                () -> usuarios.crear(new Usuario(null, hotelDos.getId(), "intruso", "clave",
                        RolUsuario.RECEPCIONISTA)),
                "El Proxy debe rechazar entidades de otro tenant");

        Habitacion habitacionUno = habitacionControlador.crear("101", TipoHabitacion.DOBLE,
                new BigDecimal("80.00"), 2, true, true);
        Cliente clienteUno = clienteControlador.crear("Ana Uno", "DOC-1", "999");
        Reserva reservaUno = reservaControlador.crear(habitacionUno.getId(), clienteUno.getId(),
                ahora.plusDays(1), ahora.plusDays(2), BigDecimal.ZERO);

        verificar(reservaUno.getId() != null, "ReservaDAO debe devolver el id generado");
        verificar(reservaControlador.registrarPago(reservaUno.getId(), new BigDecimal("25.00"))
                .getTotalPagado().compareTo(new BigDecimal("25.00")) == 0,
                "El servicio debe registrar y persistir pagos");
        verificar(habitaciones.buscarPorNumero("101").isPresent(), "Debe buscar habitacion por numero");
        verificar(clientes.buscarPorDocumento("DOC-1").isPresent(), "Debe buscar cliente por documento");

        esperarExcepcion(ReglaNegocioException.class,
                () -> habitacionControlador.crear("101", TipoHabitacion.INDIVIDUAL,
                        new BigDecimal("40.00"), 1, false, false),
                "El servicio debe rechazar numeros de habitacion duplicados en el tenant");

        habitacionControlador.ocupar(habitacionUno.getId());
        verificar(habitaciones.buscarPorId(habitacionUno.getId()).orElseThrow().getEstado()
                == EstadoHabitacion.OCUPADA, "El DAO debe persistir el estado resultante de State");

        Usuario principalDos = new Usuario(null, hotelDos.getId(), "admin2", "texto-plano",
                RolUsuario.ADMINISTRADOR);
        sesion.iniciar(principalDos);
        usuarios.crear(principalDos);

        verificar(habitaciones.listar().isEmpty(), "El listado no debe filtrar datos del otro tenant");
        verificar(habitaciones.buscarPorId(habitacionUno.getId()).isEmpty(),
                "La busqueda por id no debe revelar datos del otro tenant");
        verificar(!habitaciones.eliminar(habitacionUno.getId()),
                "La eliminacion no debe afectar datos del otro tenant");

        Habitacion habitacionDos = habitaciones.crear(nuevaHabitacion(hotelDos.getId(), "101"));
        Cliente clienteDos = clientes.crear(new Cliente(null, hotelDos.getId(), "Beto Dos", "DOC-1", null));

        esperarExcepcion(DAOException.class,
                () -> reservas.crear(new Reserva(null, hotelDos.getId(), habitacionUno.getId(),
                        clienteDos.getId(), ahora.plusDays(3), ahora.plusDays(4), BigDecimal.ZERO,
                        EstadoReserva.ACTIVA)),
                "PostgreSQL debe rechazar una habitacion perteneciente a otro tenant");

        Reserva reservaDos = reservas.crear(new Reserva(null, hotelDos.getId(), habitacionDos.getId(),
                clienteDos.getId(), ahora.plusDays(3), ahora.plusDays(4), new BigDecimal("10.00"),
                EstadoReserva.ACTIVA));
        verificar(reservas.listar().size() == 1, "Debe listar solo las reservas del tenant activo");
        verificar(reservas.eliminar(reservaDos.getId()), "Debe eliminar la reserva del tenant activo");
        verificar(clientes.eliminar(clienteDos.getId()), "Debe eliminar el cliente del tenant activo");
        verificar(habitaciones.eliminar(habitacionDos.getId()), "Debe eliminar la habitacion del tenant activo");

        Hotel hotelDosActualizado = new Hotel(hotelDos.getId(), "Hotel Dos Actualizado", hotelDos.getRuc(),
                hotelDos.getDireccion(), hotelDos.getCreadoEn());
        verificar(hoteles.actualizar(hotelDosActualizado), "HotelDAO debe actualizar la raiz multitenant");
    }

    private static Habitacion nuevaHabitacion(int hotelId, String numero) {
        return new HabitacionBuilder()
                .paraHotel(hotelId)
                .conNumero(numero)
                .deTipo(TipoHabitacion.DOBLE)
                .conPrecioPorNoche(new BigDecimal("80.00"))
                .conCantidadCamas(2)
                .conBanoPrivado()
                .conTv()
                .construir();
    }

    private static void crearEsquema(ConexionBD conexionBase, String esquema) throws Exception {
        try (Connection conexion = conexionBase.obtenerConexion(); Statement sentencia = conexion.createStatement()) {
            sentencia.execute("CREATE SCHEMA " + esquema);
            sentencia.execute("SET search_path TO " + esquema);

            String sql = Files.readString(Path.of("src/main/resources/db/schema.sql"))
                    .replaceFirst("(?im)^\\s*CREATE\\s+DATABASE\\s+sistema_hotel\\s*;", "");
            for (String bloque : sql.split(";")) {
                if (!bloque.isBlank()) {
                    sentencia.execute(bloque);
                }
            }
        }
    }

    private static Connection conexionEnEsquema(ConexionBD conexionBase, String esquema) throws SQLException {
        Connection conexion = conexionBase.obtenerConexion();
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("SET search_path TO " + esquema);
        }
        return conexion;
    }

    private static void eliminarEsquema(ConexionBD conexionBase, String esquema) throws Exception {
        try (Connection conexion = conexionBase.obtenerConexion(); Statement sentencia = conexion.createStatement()) {
            sentencia.execute("DROP SCHEMA IF EXISTS " + esquema + " CASCADE");
        }
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static <T extends Throwable> void esperarExcepcion(
            Class<T> tipo, Accion accion, String mensaje) {
        try {
            accion.ejecutar();
        } catch (Throwable error) {
            if (tipo.isInstance(error)) {
                return;
            }
            throw new AssertionError(mensaje + ". Excepcion inesperada: " + error, error);
        }
        throw new AssertionError(mensaje + ". No se produjo " + tipo.getSimpleName());
    }

    @FunctionalInterface
    private interface Accion {
        void ejecutar();
    }
}
