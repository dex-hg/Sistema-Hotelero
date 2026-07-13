package hotel.modelo.entidades;

import hotel.modelo.entidades.constantes.EstadoReserva;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ReservaTest {

    @Test
    void debeSepararTotalHospedajeDeMontoPagado() {
        Reserva reserva = nuevaReserva(
                new BigDecimal("100.00"),
                new BigDecimal("10.00")
        );

        reserva.registrarPago(new BigDecimal("20.00"));

        assertEquals(new BigDecimal("100.00"), reserva.getTotalHospedaje());
        assertEquals(new BigDecimal("30.00"), reserva.getMontoPagado());
    }

    @Test
    void debeRechazarPagosMayoresAlSaldoPendiente() {
        Reserva reserva = nuevaReserva(
                new BigDecimal("100.00"),
                new BigDecimal("30.00")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reserva.registrarPago(new BigDecimal("70.01"))
        );
    }

    private Reserva nuevaReserva(
            BigDecimal totalHospedaje,
            BigDecimal montoPagado
    ) {
        LocalDateTime ingreso = LocalDateTime.of(2026, 7, 13, 12, 0);
        return new Reserva(
                1,
                1,
                1,
                1,
                ingreso,
                ingreso.plusDays(1),
                totalHospedaje,
                montoPagado,
                EstadoReserva.ACTIVA
        );
    }
}
