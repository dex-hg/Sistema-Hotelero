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
    
    // Credenciales de Prueba
    /**
     * INSERT INTO hoteles (nombre, ruc, direccion) VALUES
     * ('Hotel Central', '20123456789', 'Av. Principal 1000, Lima'),
     * ('Hostal del Sol', '20987654321', 'Calle Las Magnolias 250, Arequipa'),
     * ('Resort Las Dunas', '20456123789', 'Panamericana Sur Km 300, Ica');
     *
     * INSERT INTO usuarios (hotel_id, username, password, rol) VALUES
     * (1, 'jadmin_central', 'root', 'ADMINISTRADOR'),
     * (1, 'mrecep_central', 'root', 'RECEPCIONISTA'),
     * (2, 'admin_sol', 'root', 'ADMINISTRADOR'),
     * (2, 'recep_sol1', 'root', 'RECEPCIONISTA'),
     * (2, 'recep_sol2', 'root', 'RECEPCIONISTA'),
     * (3, 'admin_dunas', 'root', 'ADMINISTRADOR');
     */
}
