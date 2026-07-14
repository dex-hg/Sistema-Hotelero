# Sistema de gestión hotelera HostelFlow 

HostelFlow es una aplicación de escritorio para administrar las operaciones
principales de hoteles, hostales y alojamientos. El sistema fue desarrollado en
Java con Swing, PostgreSQL y JDBC, siguiendo una arquitectura por capas basada
en MVC y DAO.

## Objetivo del proyecto

El objetivo es centralizar y simplificar la gestión diaria de un alojamiento:
habitaciones, huéspedes, reservas, recepción, check-in, check-out y reportes.
La aplicación mantiene aislada la información de cada hotel mediante
un contexto de sesión multi-tenant y ofrece funciones diferentes según el rol
del usuario.

El proyecto también busca demostrar la aplicación práctica de buenas prácticas
de desarrollo, los principios SOLID y los patrones de diseño Builder, Proxy y
State en una aplicación Java conectada a una base de datos relacional.

## Funcionalidades principales

- Inicio de sesión mediante RUC, usuario y contraseña.
- Separación de permisos entre administrador y recepcionista.
- Registro, actualización, consulta y eliminación de habitaciones.
- Control del estado operativo de las habitaciones.
- Registro y administración de huéspedes.
- Creación de reservas con cálculo automático del total de hospedaje.
- Asociación de un huésped principal y huéspedes adicionales.
- Prevención de reservas solapadas y de huéspedes en varias reservas activas.
- Registro de check-in, check-out, cancelaciones y pagos.
- Finalización de reservas vencidas.
- Dashboard operativo y reportes administrativos.
- Aislamiento de información por hotel.

## Tecnologías utilizadas

- Java 23.
- Maven.
- Swing y FlatLaf 3.6.
- PostgreSQL 42.7.11 mediante JDBC.
- JUnit 5 para pruebas automatizadas.

## Arquitectura

El código se organiza en capas con responsabilidades diferenciadas:

```text
hotel
|-- configuracion     Raíz de composición e inyección de dependencias
|-- conexion          Conexiones y ejecución transaccional
|-- controlador       Comunicación entre las vistas y los servicios
|-- dao               Contratos de persistencia
|   |-- jdbc          Implementaciones JDBC
|-- modelo
|   |-- entidades     Entidades y enumeraciones del dominio
|   |-- seguridad     Autorización según la sesión y el rol
|   |-- servicio      Reglas y casos de uso
|   |-- sesion        Contexto multi-tenant
|-- patrones          Implementaciones de Builder, Proxy y State
|-- vista             Interfaz gráfica Swing
```

La clase
[`ComposicionAplicacion`](src/main/java/hotel/configuracion/ComposicionAplicacion.java)
construye el grafo de dependencias. Las vistas dependen de controladores, los
controladores de interfaces de servicio y los servicios de interfaces DAO. Las
implementaciones JDBC permanecen aisladas de la interfaz gráfica y de las
entidades.

## Patrones de diseño aplicados

### Builder

El patrón Builder se implementa en
[`HabitacionBuilder`](src/main/java/hotel/patrones/creacional/HabitacionBuilder.java).
Permite construir una habitación paso a paso con una API fluida, evitando un
constructor difícil de leer con numerosos parámetros.

Ejemplo:

```java
Habitacion habitacion = new HabitacionBuilder()
        .paraHotel(hotelId)
        .conNumero("101")
        .deTipo(TipoHabitacion.DOBLE)
        .conPrecioPorNoche(new BigDecimal("80.00"))
        .conCantidadCamas(2)
        .conBanoPrivado()
        .conTv()
        .construir();
```

### Proxy

El patrón Proxy protege el acceso a los DAO y mantiene el aislamiento entre
hoteles. Antes de delegar al DAO JDBC, cada Proxy comprueba que exista una
sesión y que la entidad pertenezca al hotel activo.

Implementaciones:

- [`UsuarioDAOProxy`](src/main/java/hotel/patrones/estructural/UsuarioDAOProxy.java)
- [`HabitacionDAOProxy`](src/main/java/hotel/patrones/estructural/HabitacionDAOProxy.java)
- [`ClienteDAOProxy`](src/main/java/hotel/patrones/estructural/ClienteDAOProxy.java)
- [`ReservaDAOProxy`](src/main/java/hotel/patrones/estructural/ReservaDAOProxy.java)

`UsuarioDAOProxy` también protege las operaciones administrativas de usuarios.
La autorización general por rol se complementa con
[`AutorizadorSesion`](src/main/java/hotel/modelo/seguridad/AutorizadorSesion.java)
en la capa de servicios.

### State

El patrón State controla el ciclo de vida de una habitación. La entidad
[`Habitacion`](src/main/java/hotel/modelo/entidades/Habitacion.java) delega las
transiciones a una implementación de
[`EstadoHabitacionState`](src/main/java/hotel/patrones/comportamiento/EstadoHabitacionState.java).

Estados concretos:

- `DisponibleState`: permite ocupar la habitación o enviarla a mantenimiento.
- `OcupadaState`: permite iniciar la limpieza después de la estadía.
- `EnLimpiezaState`: permite habilitar la habitación o enviarla a mantenimiento.
- `MantenimientoState`: obliga a pasar por limpieza antes de volver a estar disponible.

Las transiciones válidas son:

```text
DISPONIBLE --> OCUPADA --> EN_LIMPIEZA --> DISPONIBLE
     |                           | 
     |--> MANTENIMIENTO <--------|
               |
               |--> EN_LIMPIEZA
```

Una transición no permitida produce una
`TransicionEstadoHabitacionException` y no se persiste.

## Aplicación de SOLID

| Principio | Aplicación en HostelFlow | Dónde se puede observar |
|---|---|---|
| **SRP** — Responsabilidad única | La presentación, los casos de uso, la persistencia y las entidades están separados. | Paquetes `vista`, `controlador`, `modelo.servicio` y `dao.jdbc`. |
| **OCP** — Abierto/cerrado | Las reglas de transición se extienden mediante implementaciones State sin añadir condicionales de transición a `Habitacion`. | `EstadoHabitacionState` y sus cuatro estados concretos. |
| **LSP** — Sustitución de Liskov | Un DAO JDBC puede ser sustituido por su Proxy porque ambos implementan la misma interfaz DAO. | `ClienteDAOJdbc`/`ClienteDAOProxy`, `HabitacionDAOJdbc`/`HabitacionDAOProxy`, entre otros. |
| **ISP** — Segregación de interfaces | Las clases reciben contratos pequeños y específicos en lugar de interfaces generales con métodos que no utilizan. | `ProveedorHotelId`, `AutorizadorAcceso`, `EjecutorTransaccional` y `PanelActualizable`. |
| **DIP** — Inversión de dependencias | Controladores y servicios dependen de interfaces; la creación de implementaciones concretas se concentra en un único lugar. | Servicios `*ServicioImpl`, controladores y `ComposicionAplicacion`. |

### SRP — Single Responsibility Principle

Cada capa tiene un motivo de cambio diferente:

- [`PanelRecepcion`](src/main/java/hotel/vista/panel/PanelRecepcion.java)
  presenta datos y captura las acciones del usuario.
- [`ReservaControlador`](src/main/java/hotel/controlador/ReservaControlador.java)
  expone los casos de uso a la vista sin conocer JDBC.
- [`ReservaServicioImpl`](src/main/java/hotel/modelo/servicio/impl/ReservaServicioImpl.java)
  aplica reglas de reservas, pagos, huéspedes y transacciones.
- [`ReservaDAOJdbc`](src/main/java/hotel/dao/jdbc/ReservaDAOJdbc.java)
  traduce las operaciones de persistencia a SQL.
- [`EstadisticasServicioImpl`](src/main/java/hotel/modelo/servicio/impl/EstadisticasServicioImpl.java)
  concentra los cálculos del dashboard y los reportes fuera de Swing.

De este modo, un cambio visual no obliga a modificar SQL y un cambio de base de
datos no debería afectar directamente a los paneles.

### OCP — Open/Closed Principle

[`EstadoHabitacionState`](src/main/java/hotel/patrones/comportamiento/EstadoHabitacionState.java)
define el contrato común y cada estado concreto encapsula sus propias
transiciones. Por ejemplo, `OcupadaState` solo permite iniciar limpieza,
mientras que `EnLimpiezaState` permite habilitar la habitación o enviarla a
mantenimiento.

La aplicación de OCP se limita a las reglas de transición: estas pueden
modificarse o extenderse en clases State sin llenar `Habitacion` de
condicionales. Agregar un valor completamente nuevo al enum todavía requiere
actualizar la fábrica que reconstruye el estado en `Habitacion`.

### LSP — Liskov Substitution Principle

Las implementaciones JDBC y los Proxy cumplen las mismas interfaces:

- [`ClienteDAO`](src/main/java/hotel/dao/ClienteDAO.java) es implementado por
  `ClienteDAOJdbc` y `ClienteDAOProxy`.
- [`HabitacionDAO`](src/main/java/hotel/dao/HabitacionDAO.java) es implementado
  por `HabitacionDAOJdbc` y `HabitacionDAOProxy`.
- [`ReservaDAO`](src/main/java/hotel/dao/ReservaDAO.java) es implementado por
  `ReservaDAOJdbc` y `ReservaDAOProxy`.
- [`UsuarioDAO`](src/main/java/hotel/dao/UsuarioDAO.java) es implementado por
  `UsuarioDAOJdbc` y `UsuarioDAOProxy`.

Los servicios trabajan con la interfaz DAO, mientras la raíz de composición
puede entregar el Proxy que envuelve al DAO JDBC sin cambiar el código cliente.
El Proxy conserva el contrato de persistencia y añade las verificaciones de
sesión, tenant y autorización correspondientes.

### ISP — Interface Segregation Principle

Se utilizan contratos reducidos para que cada consumidor dependa únicamente de
lo que necesita:

- [`ProveedorHotelId`](src/main/java/hotel/modelo/sesion/ProveedorHotelId.java)
  solo proporciona el identificador del hotel activo.
- [`AutorizadorAcceso`](src/main/java/hotel/modelo/seguridad/AutorizadorAcceso.java)
  solo expone la verificación administrativa.
- [`EjecutorTransaccional`](src/main/java/hotel/conexion/EjecutorTransaccional.java)
  solo representa la ejecución atómica de una operación.
- [`PanelActualizable`](src/main/java/hotel/vista/panel/PanelActualizable.java)
  contiene únicamente el contrato necesario para refrescar un panel.

### DIP — Dependency Inversion Principle

Los módulos de negocio no construyen ni dependen directamente de clases JDBC.
Por ejemplo, `ReservaServicioImpl` recibe `ReservaDAO`, `HabitacionDAO`,
`ClienteDAO`, `ProveedorHotelId`, `EjecutorTransaccional` y
`AutorizadorAcceso` mediante su constructor. De forma similar, cada controlador
recibe una interfaz de servicio.

[`ComposicionAplicacion`](src/main/java/hotel/configuracion/ComposicionAplicacion.java)
es el único punto responsable de elegir las implementaciones concretas, crear
los DAO JDBC, envolverlos con Proxy y conectarlos con servicios, controladores
y vistas.

## Base de datos

La configuración predeterminada se encuentra en
[`ConexionConfig`](src/main/java/hotel/conexion/ConexionConfig.java):

```text
Base de datos: sistema_hotel
Servidor:      localhost:5432
Usuario:       postgres
Contraseña:    root
```

Para una instalación nueva:

1. Iniciar PostgreSQL.
2. Ejecutar `CREATE DATABASE sistema_hotel;` de la primera sentencia de
   [`src/main/resources/db/schema.sql`](src/main/resources/db/schema.sql) 
   desde una conexión administrativa.
3. Conectarse a `sistema_hotel`.
4. Ejecutar el resto del conetenido de 
    [`src/main/resources/db/schema.sql`](src/main/resources/db/schema.sql)

El esquema incluye tablas, claves foráneas multi-tenant, restricciones,
índices y datos de demostración.

## Credenciales de demostración

| RUC | Usuario | Rol | Contraseña |
|---|---|---|---|
| `20123456789` | `jadmin_central` | Administrador | `root` |
| `20123456789` | `mrecep_central` | Recepcionista | `root` |
| `20987654321` | `admin_sol` | Administrador | `root` |
| `20987654321` | `recep_sol1` | Recepcionista | `root` |
| `20987654321` | `recep_sol2` | Recepcionista | `root` |
| `20456123789` | `admin_dunas` | Administrador | `root` |

Estas credenciales son únicamente datos de prueba para el entorno académico.

## Ejecución

Desde la raíz del proyecto:

```powershell
mvn compile exec:java
```

También puede ejecutarse `hotel.HostelFlowAplicacion` directamente desde
NetBeans.

## Pruebas

Para compilar y ejecutar toda la suite:

```powershell
mvn test
```

La cobertura actual incluye:

- Integración de DAO JDBC, Proxy y PostgreSQL.
- Aislamiento multi-tenant.
- Permisos por rol.
- Atomicidad y rollback de reservas.
- Prevención de reservas solapadas y huéspedes duplicados.
- Separación entre total del hospedaje y monto pagado.
- Transiciones válidas e inválidas del patrón State.

Las pruebas de integración crean y eliminan un esquema temporal, por lo que no
modifican los datos existentes de `sistema_hotel`.
