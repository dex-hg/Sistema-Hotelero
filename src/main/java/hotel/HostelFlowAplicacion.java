package hotel;

import hotel.configuracion.ComposicionAplicacion;
import hotel.vista.ventana.VistaSwing;

public final class HostelFlowAplicacion {

    private HostelFlowAplicacion() {
    }

    public static void main(String[] args) {
        ComposicionAplicacion aplicacion = new ComposicionAplicacion();
        new VistaSwing(aplicacion).iniciar(); 
    }
    
    // Credenciales de Prueba
    /**
     * RUC: 20123456789
     * 
     * Usuario: jadmin_central
     * Rol: Administrador
     * Contraseña: root
     * 
     * Usuario: mrecep_central
     * Rol: Recepcionista
     * Contraseña: root
     */
}
