package hotel.vista.ventana;

import hotel.configuracion.ComposicionAplicacion;
import hotel.vista.EstilosSwing;

import java.util.Objects;
import javax.swing.SwingUtilities;

public final class VistaSwing {

    private final ComposicionAplicacion aplicacion;

    public VistaSwing(ComposicionAplicacion aplicacion) {
        this.aplicacion = Objects.requireNonNull(aplicacion);
    }

    public void iniciar() {
        SwingUtilities.invokeLater(() -> {
            EstilosSwing.aplicar();
            new VentanaLogin(aplicacion).mostrar();
        });
    }
}
