package hotel.vista;

import hotel.excepcion.DAOException;

import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
                    "Ingrese un número válido"
            );
        } catch (DateTimeParseException e) {
            mostrarError(
                    padre,
                    "Ingrese una fecha válida "
                    + "con formato yyyy-MM-dd HH:mm"
            );
        } catch (IllegalArgumentException e) {
            mostrarError(padre, mensajeUsuario(e));
        } catch (RuntimeException e) {
            mostrarError(padre, mensajeUsuario(e));
        }
    }

    public static void ejecutarConExito(
            Component padre,
            Runnable accion,
            String mensajeExito
    ) {
        ejecutar(padre, () -> {
            accion.run();
            mostrarInfo(padre, mensajeExito);
        });
    }

    public static <T> void ejecutarAsync(
            Component padre,
            JButton boton,
            Supplier<T> tarea,
            Consumer<T> alCompletar,
            String mensajeExito
    ) {
        if (boton != null) {
            boton.setEnabled(false);
        }
        padre.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<T, Void>() {
            private boolean interfazRestaurada;

            @Override
            protected T doInBackground() {
                return tarea.get();
            }

            @Override
            protected void done() {
                try {
                    T resultado = get();
                    restaurarInterfaz();
                    alCompletar.accept(resultado);
                    if (mensajeExito != null && !mensajeExito.isBlank()) {
                        mostrarInfo(padre, mensajeExito);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError(padre, "La operación fue interrumpida");
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();
                    if (causa instanceof RuntimeException runtime) {
                        mostrarError(padre, mensajeUsuario(runtime));
                    } else {
                        mostrarError(padre, "No se pudo completar la operación");
                    }
                } catch (RuntimeException e) {
                    mostrarError(padre, mensajeUsuario(e));
                } finally {
                    if (!interfazRestaurada) {
                        restaurarInterfaz();
                    }
                }
            }

            private void restaurarInterfaz() {
                padre.setCursor(Cursor.getDefaultCursor());
                if (boton != null) {
                    boton.setEnabled(true);
                }
                interfazRestaurada = true;
            }
        }.execute();
    }

    public static boolean confirmar(
            Component padre,
            String mensaje,
            String titulo
    ) {
        return JOptionPane.showConfirmDialog(
                padre,
                mensaje,
                titulo,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }

    public static void exigirExito(boolean resultado, String mensaje) {
        if (!resultado) {
            throw new IllegalStateException(mensaje);
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

    public static JTextField campoLectura(String valorInicial) {
        JTextField campo = campo(valorInicial);
        campo.setEditable(false);
        return campo;
    }

    public static JPanel encabezadoModulo(
            String titulo,
            String subtitulo,
            JButton accion
    ) {
        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(
                tituloLabel.getFont().deriveFont(Font.BOLD, 24f)
        );

        JLabel subtituloLabel = new JLabel(subtitulo);
        subtituloLabel.setForeground(new Color(87, 99, 115));

        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 4));
        textos.setOpaque(false);
        textos.add(tituloLabel);
        textos.add(subtituloLabel);

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.add(textos, BorderLayout.WEST);
        if (accion != null) {
            panel.add(envolverBotonCompacto(accion), BorderLayout.EAST);
        }
        return panel;
    }

    public static JPanel envolverBotonCompacto(JButton boton) {
        JPanel contenedor = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        contenedor.setOpaque(false);
        contenedor.add(boton);
        return contenedor;
    }

    public static void fijarAltoBoton(JButton boton, int alto) {
        Dimension preferido = boton.getPreferredSize();
        Dimension dimension = new Dimension(preferido.width, alto);
        boton.setPreferredSize(dimension);
        boton.setMinimumSize(dimension);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
    }

    public static JPanel seccion(String titulo, Component contenido) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 232)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        if (titulo != null && !titulo.isBlank()) {
            JLabel etiqueta = new JLabel(titulo);
            etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD));
            panel.add(etiqueta, BorderLayout.NORTH);
        }
        panel.add(contenido, BorderLayout.CENTER);
        return panel;
    }

    public static JPanel tarjetaResumen(
            String titulo,
            String valor,
            String codigo,
            Color color
    ) {
        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setForeground(new Color(87, 99, 115));
        tituloLabel.setFont(tituloLabel.getFont().deriveFont(Font.BOLD));

        JLabel valorLabel = new JLabel(valor);
        valorLabel.setFont(valorLabel.getFont().deriveFont(Font.BOLD, 28f));

        JLabel codigoLabel = new JLabel(codigo);
        codigoLabel.setForeground(color);
        codigoLabel.setFont(codigoLabel.getFont().deriveFont(Font.BOLD, 20f));

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.setOpaque(false);
        centro.add(tituloLabel, BorderLayout.NORTH);
        centro.add(valorLabel, BorderLayout.WEST);
        centro.add(codigoLabel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, color),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));
        panel.setBackground(Color.WHITE);
        panel.add(centro, BorderLayout.CENTER);
        return panel;
    }

    public static JButton botonPrimario(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(new Color(25, 118, 210));
        boton.setForeground(Color.WHITE);
        return boton;
    }

    public static JButton botonSecundario(String texto) {
        return new JButton(texto);
    }

    public static JButton botonPeligro(String texto) {
        JButton boton = botonSecundario(texto);
        boton.setForeground(new Color(190, 45, 55));
        return boton;
    }

    public static JButton botonCompacto(String texto) {
        JButton boton = botonSecundario(texto);
        boton.setMargin(new Insets(2, 12, 2, 12));
        boton.putClientProperty("JButton.buttonType", "roundRect");
        fijarAltoBoton(boton, 30);
        Dimension tamano = boton.getPreferredSize();
        Dimension ampliado = new Dimension(tamano.width + 24, tamano.height);
        boton.setPreferredSize(ampliado);
        boton.setMinimumSize(ampliado);
        return boton;
    }

    public static JButton botonPeligroCompacto(String texto) {
        JButton boton = botonCompacto(texto);
        boton.setForeground(new Color(190, 45, 55));
        return boton;
    }

    public static DefaultTableModel modeloTabla(Object[] columnas) {
        Class<?>[] tipos = new Class<?>[columnas.length];
        java.util.Arrays.fill(tipos, Object.class);
        return modeloTabla(columnas, tipos);
    }

    public static DefaultTableModel modeloTabla(
            Object[] columnas,
            Class<?>[] tipos
    ) {
        if (columnas.length != tipos.length) {
            throw new IllegalArgumentException(
                    "Cada columna debe declarar su tipo"
            );
        }
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                return tipos[columna];
            }
        };
    }

    public static TableRowSorter<DefaultTableModel> configurarTabla(
            JTable tabla,
            DefaultTableModel modelo
    ) {
        TableRowSorter<DefaultTableModel> ordenador
                = new TableRowSorter<>(modelo);
        tabla.setRowSorter(ordenador);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowSelectionAllowed(true);
        tabla.setColumnSelectionAllowed(false);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setDefaultRenderer(Enum.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object valor) {
                setText(valor instanceof Enum<?> enumerado
                        ? textoEnum(enumerado) : String.valueOf(valor));
            }
        });
        return ordenador;
    }

    public static String textoEnum(Enum<?> valor) {
        String texto = valor.name().replace('_', ' ').toLowerCase();
        if (texto.isEmpty()) {
            return texto;
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    public static void conectarBusqueda(
            JTextField busqueda,
            TableRowSorter<DefaultTableModel> ordenador
    ) {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarBusqueda(busqueda, ordenador);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarBusqueda(busqueda, ordenador);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarBusqueda(busqueda, ordenador);
            }
        };
        busqueda.getDocument().addDocumentListener(listener);
    }

    public static void alCambiarTexto(JTextField campo, Runnable accion) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                accion.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                accion.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                accion.run();
            }
        });
    }

    public static void conectarBusquedaPorColumna(
            JTextField busqueda,
            TableRowSorter<DefaultTableModel> ordenador,
            int columna
    ) {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarBusqueda(busqueda, ordenador, columna);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarBusqueda(busqueda, ordenador, columna);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarBusqueda(busqueda, ordenador, columna);
            }
        };
        busqueda.getDocument().addDocumentListener(listener);
    }

    public static Integer idFilaSeleccionada(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }

        Object valor = tabla.getModel().getValueAt(
                tabla.convertRowIndexToModel(fila),
                0
        );
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        return entero(String.valueOf(valor));
    }

    public static int idSeleccionadoOPedido(
            Component padre,
            JTable tabla,
            String etiqueta
    ) {
        Integer id = idFilaSeleccionada(tabla);
        if (id != null) {
            return id;
        }

        return entero(
                pedirTexto(
                        padre,
                        etiqueta
                )
        );
    }

    private static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(
                padre,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static void mostrarInfo(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(
                padre,
                mensaje,
                "Operación completada",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static String mensajeUsuario(RuntimeException e) {
        if (e instanceof DAOException) {
            return "No se pudo comunicar con la base de datos. "
                    + "Verifique la conexión e inténtelo nuevamente";
        }
        String mensaje = e.getMessage();
        
        if (mensaje == null || mensaje.isBlank()) {
            return "No se pudo completar la operación";
        }

        if (mensaje.startsWith("For input string")) {
            return "Ingrese un número válido";
        }

        return mensaje;
    }

    private static void aplicarBusqueda(
            JTextField busqueda,
            TableRowSorter<DefaultTableModel> ordenador
    ) {
        String texto = busqueda.getText().trim();
        if (texto.isBlank()) {
            ordenador.setRowFilter(null);
            return;
        }

        ordenador.setRowFilter(
                RowFilter.regexFilter(
                        "(?i)" + Pattern.quote(texto)
                )
        );
    }

    private static void aplicarBusqueda(
            JTextField busqueda,
            TableRowSorter<DefaultTableModel> ordenador,
            int columna
    ) {
        String texto = busqueda.getText().trim();
        if (texto.isBlank()) {
            ordenador.setRowFilter(null);
            return;
        }

        ordenador.setRowFilter(
                RowFilter.regexFilter(
                        "(?i)" + Pattern.quote(texto),
                        columna
                )
        );
    }
}
