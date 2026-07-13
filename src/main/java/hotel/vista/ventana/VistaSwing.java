package hotel.vista;

import hotel.configuracion.ComposicionAplicacion;

import java.util.Objects;

public final class VistaSwing {

    private final ComposicionAplicacion aplicacion;

    public VistaSwing(ComposicionAplicacion aplicacion) {
        this.aplicacion = Objects.requireNonNull(aplicacion);
    }

    public void iniciar() {
        EstilosSwing.aplicar();
        new VentanaLogin(aplicacion).mostrar();
    }
}
