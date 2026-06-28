package hotel;

import hotel.configuracion.ComposicionAplicacion;
import hotel.vista.VistaSwing;

public final class HostelFlowAplicacion {

    private HostelFlowAplicacion() {
    }

    public static void main(String[] args) {
        ComposicionAplicacion aplicacion = new ComposicionAplicacion();
        new VistaSwing(aplicacion).iniciar(); 
    }
}
