package hotel.vista;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public final class EstilosSwing {

    private EstilosSwing() {
    }

    public static void aplicar() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("TextComponent.arc", 12);
        } catch (UnsupportedLookAndFeelException e) {
            throw new IllegalStateException("No se pudo aplicar FlatLaf", e);
        }
    }
}
