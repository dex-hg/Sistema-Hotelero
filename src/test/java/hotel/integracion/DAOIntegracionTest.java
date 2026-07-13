package hotel.integracion;

import hotel.conexion.ConexionBD;
import hotel.conexion.ConexionConfig;
import hotel.conexion.ProveedorConexion;
import hotel.configuracion.ComposicionAplicacion;
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
import hotel.modelo.servicio.DatosHuespedRecepcion;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

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

        ComposicionAplicacion aplicacion = new ComposicionAplicacion(conexiones, new ContextoSesion());
        esperarExcepcion(SesionNoIniciadaException.class,
                () -> aplicacion.habitacionControlador().listar(),
                "La composicion debe entregar controladores protegidos por Proxy");
        esperarExcepcion(ReglaNegocioException.class,
                () -> aplicacion.autenticacionControlador().iniciarSesion(
                        hotelUno.getRuc(),
                        "admin1",
                        "clave-incorrecta"),
                "La autenticacion debe rechazar contrasenas incorrectas");

        Usuario autenticado = aplicacion.autenticacionControlador().iniciarSesion(
                hotelUno.getRuc(),
                "admin1",
                "texto-plano");
        verificar(autenticado.getId().equals(usuarioUno.getId()),
                "La autenticacion por RUC debe iniciar sesion con el usuario persistido");
        verificar(aplicacion.contextoSesion().getHotelId() == hotelUno.getId(),
                "La autenticacion debe fijar el hotel activo en el contexto de sesion");

        Habitacion habitacionAdmin = aplicacion.habitacionControlador().crear(
                "301", TipoHabitacion.INDIVIDUAL, new BigDecimal("55.00"), 1, false, true);
        Habitacion habitacionAdminActualizada = new HabitacionBuilder()
                .conId(habitacionAdmin.getId())
                .paraHotel(hotelUno.getId())
                .conNumero("301-A")
                .deTipo(TipoHabitacion.MATRIMONIAL)
                .conPrecioPorNoche(new BigDecimal("75.00"))
                .conCantidadCamas(1)
                .conTv()
                .construir();
        verificar(aplicacion.habitacionControlador().actualizar(habitacionAdminActualizada),
                "El administrador debe poder modificar habitaciones");
        verificar(aplicacion.habitacionControlador().buscarPorNumero("301-A").isPresent(),
                "Debe persistirse la modificacion de habitacion");
        verificar(aplicacion.habitacionControlador().eliminar(habitacionAdmin.getId()),
                "El administrador debe poder eliminar habitaciones");

        Habitacion habitacionTransaccional = aplicacion.habitacionControlador().crear(
                "201", TipoHabitacion.DOBLE, new BigDecimal("90.00"), 2, true, true);
        Cliente clienteTransaccional = aplicacion.clienteControlador().crear(
                "Cliente Transaccional", "DOC-TX", "988");
        Reserva reservaTransaccional = aplicacion.reservaControlador().crear(
                habitacionTransaccional.getId(), clienteTransaccional.getId(),
                ahora.plusDays(5), ahora.plusDays(6), BigDecimal.ZERO);

        aplicacion.reservaControlador().registrarCheckIn(reservaTransaccional.getId());
        verificar(aplicacion.habitacionControlador().buscarPorId(habitacionTransaccional.getId())
                .getEstado() == EstadoHabitacion.OCUPADA,
                "El check-in debe ocupar la habitacion dentro del tenant activo");

        Reserva reservaFinalizada = aplicacion.reservaControlador().registrarCheckOut(
                reservaTransaccional.getId());
        verificar(reservaFinalizada.getEstado() == EstadoReserva.FINALIZADA,
                "El check-out debe finalizar atomicamente una reserva activa");
        verificar(aplicacion.habitacionControlador().buscarPorId(habitacionTransaccional.getId())
                .getEstado() == EstadoHabitacion.EN_LIMPIEZA,
                "El check-out debe enviar la habitacion a limpieza");

        Habitacion habitacionVencida = aplicacion.habitacionControlador().crear(
                "205", TipoHabitacion.INDIVIDUAL, new BigDecimal("60.00"),
                1, false, false);
        Reserva reservaVencida = aplicacion.reservaControlador().crear(
                habitacionVencida.getId(), clienteTransaccional.getId(),
                ahora.minusDays(1), ahora.minusMinutes(1), BigDecimal.ZERO);
        aplicacion.reservaControlador().registrarCheckIn(reservaVencida.getId());
        verificar(aplicacion.reservaControlador().finalizarVencidas() >= 1,
                "Debe finalizar automáticamente reservas cuya salida ya venció");
        verificar(aplicacion.reservaControlador().buscarPorId(reservaVencida.getId())
                .getEstado() == EstadoReserva.FINALIZADA,
                "La reserva vencida debe quedar FINALIZADA");
        verificar(aplicacion.habitacionControlador().buscarPorId(habitacionVencida.getId())
                .getEstado() == EstadoHabitacion.EN_LIMPIEZA,
                "La habitación vencida debe pasar automáticamente a limpieza");
        aplicacion.reservaControlador().registrarCheckOut(reservaVencida.getId());
        verificar(aplicacion.habitacionControlador().buscarPorId(habitacionVencida.getId())
                .getEstado() == EstadoHabitacion.EN_LIMPIEZA,
                "El check-out posterior no debe repetir la transición de limpieza");
        aplicacion.habitacionControlador().habilitar(habitacionVencida.getId());
        verificar(aplicacion.habitacionControlador().buscarPorId(habitacionVencida.getId())
                .getEstado() == EstadoHabitacion.DISPONIBLE,
                "Después de limpiar se debe poder marcar la habitación disponible");
        aplicacion.habitacionControlador().enviarAMantenimiento(
                habitacionVencida.getId()
        );
        verificar(aplicacion.habitacionControlador().buscarPorId(habitacionVencida.getId())
                .getEstado() == EstadoHabitacion.MANTENIMIENTO,
                "El administrador debe poder enviar una habitación a mantenimiento");

        Habitacion habitacionRecepcion = aplicacion.habitacionControlador().crear(
                "202", TipoHabitacion.DOBLE, new BigDecimal("95.00"), 2, true, true);
        Reserva reservaRecepcion = aplicacion.reservaControlador().registrarRecepcion(
                "Cliente Transaccional Actualizado",
                "DOC-TX",
                "977",
                habitacionRecepcion.getId(),
                ahora.plusDays(7),
                ahora.plusDays(8),
                List.of(new DatosHuespedRecepcion(
                        "Acompañante Transaccional",
                        "DOC-AC",
                        "966"
                )));
        verificar(reservaRecepcion.getEstado() == EstadoReserva.ACTIVA,
                "La recepcion debe crear una reserva activa");
        verificar(contarHuespedesReserva(conexiones, hotelUno.getId(), reservaRecepcion.getId()) == 2,
                "La recepción debe asociar huésped principal y acompañantes a la reserva");
        verificar(aplicacion.reservaControlador().listarHuespedes(reservaRecepcion.getId()).size() == 2,
                "La recepción debe mostrar todos los huéspedes asociados a la reserva");
        verificar(aplicacion.reservaControlador().listarHuespedesPorReserva()
                .get(reservaRecepcion.getId()).size() == 2,
                "La recepción debe cargar huéspedes de todas las reservas en una sola consulta");
        verificar(reservaRecepcion.getTotalPagado().compareTo(new BigDecimal("95.00")) == 0,
                "La recepcion debe calcular el total segun precio por noche y dias");
        verificar(aplicacion.habitacionControlador().buscarPorId(habitacionRecepcion.getId())
                .getEstado() == EstadoHabitacion.OCUPADA,
                "La recepcion debe registrar check-in y ocupar la habitacion");
        Cliente clienteActualizado = aplicacion.clienteControlador()
                .buscarPorDocumento("DOC-TX").orElseThrow();
        verificar("Cliente Transaccional Actualizado".equals(clienteActualizado.getNombreCompleto()),
                "La recepcion debe actualizar datos del cliente existente por DNI");
        aplicacion.reservaControlador().finalizar(reservaRecepcion.getId());
        Reserva recepcionFinalizada = aplicacion.reservaControlador().registrarCheckOut(
                reservaRecepcion.getId());
        verificar(recepcionFinalizada.getEstado() == EstadoReserva.FINALIZADA,
                "El check-out de recepcion debe operar sobre reservas finalizadas");

        Habitacion habitacionCancelada = aplicacion.habitacionControlador().crear(
                "203", TipoHabitacion.INDIVIDUAL, new BigDecimal("70.00"),
                1, false, false);
        Reserva reservaCancelada = aplicacion.reservaControlador().registrarRecepcion(
                "Cliente Cancelación", "DOC-CANCEL", null,
                habitacionCancelada.getId(), ahora, ahora.plusDays(1), List.of());
        aplicacion.reservaControlador().cancelarRecepcion(reservaCancelada.getId());
        verificar(aplicacion.habitacionControlador().buscarPorId(habitacionCancelada.getId())
                .getEstado() == EstadoHabitacion.EN_LIMPIEZA,
                "Cancelar desde recepción debe enviar la habitación ocupada a limpieza");

        Habitacion habitacionCupo = aplicacion.habitacionControlador().crear(
                "204", TipoHabitacion.INDIVIDUAL, new BigDecimal("65.00"),
                1, false, false);
        esperarExcepcion(ReglaNegocioException.class,
                () -> aplicacion.reservaControlador().registrarRecepcion(
                        "Cliente Cupo", "DOC-CUPO", null,
                        habitacionCupo.getId(), ahora, ahora.plusDays(1),
                        List.of(new DatosHuespedRecepcion(
                                "Acompañante sin cupo", "DOC-CUPO-2", null))),
                "La recepción debe respetar la cantidad de camas");
        esperarExcepcion(ReglaNegocioException.class,
                () -> aplicacion.reservaControlador().registrarRecepcion(
                        "Cliente Duplicado", "DOC-DUP", null,
                        habitacionCupo.getId(), ahora, ahora.plusDays(1),
                        List.of(new DatosHuespedRecepcion(
                                "Mismo cliente", "DOC-DUP", null))),
                "La recepción debe rechazar DNI repetidos");

        aplicacion.autenticacionControlador().cerrarSesion();

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
        esperarExcepcion(ReglaNegocioException.class,
                () -> reservaControlador.crear(
                        habitacionUno.getId(), clienteUno.getId(),
                        ahora.plusHours(12), ahora.plusDays(1).plusHours(12),
                        BigDecimal.ZERO),
                "No se deben permitir reservas solapadas para la misma habitación");
        verificar(habitaciones.buscarPorNumero("101").isPresent(), "Debe buscar habitacion por numero");
        verificar(clientes.buscarPorDocumento("DOC-1").isPresent(), "Debe buscar cliente por documento");

        esperarExcepcion(ReglaNegocioException.class,
                () -> habitacionControlador.crear("101", TipoHabitacion.INDIVIDUAL,
                        new BigDecimal("40.00"), 1, false, false),
                "El servicio debe rechazar numeros de habitacion duplicados en el tenant");

        habitacionControlador.ocupar(habitacionUno.getId());
        verificar(habitaciones.buscarPorId(habitacionUno.getId()).orElseThrow().getEstado()
                == EstadoHabitacion.OCUPADA, "El DAO debe persistir el estado resultante de State");
        esperarExcepcion(ReglaNegocioException.class,
                () -> reservaControlador.crear(
                        habitacionUno.getId(), clienteUno.getId(),
                        ahora.plusDays(4), ahora.plusDays(5), BigDecimal.ZERO),
                "No se deben crear reservas en habitaciones no disponibles");

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

    private static int contarHuespedesReserva(
            ProveedorConexion conexiones,
            int hotelId,
            int reservaId
    ) {
        String sql = "SELECT COUNT(*) FROM reserva_huespedes "
                + "WHERE hotel_id = ? AND reserva_id = ?";
        try (Connection conexion = conexiones.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, hotelId);
            sentencia.setInt(2, reservaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException(
                    "Error al verificar huéspedes de reserva",
                    e
            );
        }
    }

    private static void crearEsquema(ConexionBD conexionBase, String esquema) throws Exception {
        try (Connection conexion = conexionBase.obtenerConexion(); Statement sentencia = conexion.createStatement()) {
            sentencia.execute("CREATE SCHEMA " + esquema);
            sentencia.execute("SET search_path TO " + esquema);

            String sql = Files.readString(Path.of("src/main/resources/db/schema.sql"))
                    .replaceFirst("(?im)^\\s*CREATE\\s+DATABASE\\s+sistema_hotel\\s*;", "")
                    .replaceAll("(?is)INSERT\\s+INTO\\s+hoteles\\s*\\([^;]+;", "")
                    .replaceAll("(?is)INSERT\\s+INTO\\s+usuarios\\s*\\([^;]+;", "");
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
