package hotel.modelo.entidades;

import hotel.excepcion.TransicionEstadoHabitacionException;
import hotel.modelo.entidades.constantes.EstadoHabitacion;
import hotel.modelo.entidades.constantes.TipoHabitacion;
import hotel.patrones.creacional.HabitacionBuilder;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HabitacionStateTest {

    @Test
    void debeRecorrerElCicloPermitidoDeEstados() {
        Habitacion habitacion = nuevaHabitacion();

        assertEquals(EstadoHabitacion.DISPONIBLE, habitacion.getEstado());
        assertTrue(habitacion.getTransicionesPermitidas()
                .contains(EstadoHabitacion.OCUPADA));

        habitacion.ocupar();
        habitacion.iniciarLimpieza();
        habitacion.habilitar();
        habitacion.enviarAMantenimiento();
        habitacion.iniciarLimpieza();

        assertEquals(EstadoHabitacion.EN_LIMPIEZA, habitacion.getEstado());
    }

    @Test
    void debeRechazarUnaTransicionNoPermitida() {
        Habitacion habitacion = nuevaHabitacion();

        assertThrows(
                TransicionEstadoHabitacionException.class,
                habitacion::iniciarLimpieza
        );
        assertEquals(EstadoHabitacion.DISPONIBLE, habitacion.getEstado());
    }

    private Habitacion nuevaHabitacion() {
        return new HabitacionBuilder()
                .conId(1)
                .paraHotel(1)
                .conNumero("101")
                .deTipo(TipoHabitacion.INDIVIDUAL)
                .conPrecioPorNoche(new BigDecimal("50.00"))
                .conCantidadCamas(1)
                .construir();
    }
}
