package hotel.conexion;

public final class ConexionConfig {

    public static final String URL
            = "jdbc:postgresql://localhost:5432/sistema_hotel";

    public static final String USER
            = "postgres";

    public static final String PASSWORD
            = "root";

    private ConexionConfig() {
    }

}
