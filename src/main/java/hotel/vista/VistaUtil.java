package hotel.vista;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.Component;
import java.awt.GridLayout;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class VistaUtil {

    public static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private VistaUtil() {
    }

    public static void ejecutar(Component padre, Runnable accion) {
        try {
            accion.run();
        } catch (NumberFormatException e) {
            mostrarError(
                    padre,
                    "Ingrese un numero valido"
            );
        } catch (DateTimeParseException e) {
            mostrarError(
                    padre,
                    "Ingrese una fecha valida "
                    + "con formato yyyy-MM-dd HH:mm"
            );
        } catch (IllegalArgumentException e) {
            mostrarError(padre, mensajeUsuario(e));
        } catch (RuntimeException e) {
            mostrarError(padre, mensajeUsuario(e));
        }
    }

    public static boolean confirmarFormulario(
            Component padre,
            String titulo,
            Object... campos
    ) {
        JPanel panel = new JPanel(
                new GridLayout(
                        0,
                        2,
                        10,
                        10
                )
        );

        for (int i = 0; i < campos.length; i += 2) {
            panel.add(new javax.swing.JLabel(
                    String.valueOf(campos[i])
            )
            );
            panel.add((JComponent) campos[i + 1]);
        }

        return JOptionPane.showConfirmDialog(
                padre,
                panel,
                titulo,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        ) == JOptionPane.OK_OPTION;
    }

    public static String pedirTexto(Component padre, String etiqueta) {
        String valor = JOptionPane.showInputDialog(
                padre,
                etiqueta
        );

        if (valor == null) {
            throw new IllegalArgumentException("Operacion cancelada");
        }
        return valor.trim();
    }

    public static int entero(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new NumberFormatException();
        }

        return Integer.parseInt(valor.trim());
    }

    public static BigDecimal decimal(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new NumberFormatException();
        }

        return new BigDecimal(valor.trim());
    }

    public static LocalDateTime fecha(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DateTimeParseException(
                    "Fecha vacia",
                    "",
                    0
           );
        }

        return LocalDateTime.parse(
                valor.trim(),
                FORMATO_FECHA
        );
    }

    public static JTextField campo(String valorInicial) {
        return new JTextField(valorInicial, 18);
    }

    private static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(
                padre,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static String mensajeUsuario(RuntimeException e) {
        String mensaje = e.getMessage();
        
        if (mensaje == null || mensaje.isBlank()) {
            return "No se pudo completar la operacion";
        }

        if (mensaje.startsWith("For input string")) {
            return "Ingrese un numero valido";
        }

        return mensaje;
    }
}
