package com.unillanos.editor.vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Componente que muestra los números de línea al lado izquierdo de un JTextComponent.
 */
public class LineNumberComponent extends JPanel {

    private final JTextComponent textComponent;
    private final int MIN_WIDTH = 40;

    public LineNumberComponent(JTextComponent textComponent) {
        this.textComponent = textComponent;
        setBackground(new Color(240, 240, 240));
        setBorder(new EmptyBorder(0, 5, 0, 5));
        setPreferredSize(new Dimension(MIN_WIDTH, 0));

        textComponent.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
        });

        textComponent.addCaretListener(e -> repaint());

        textComponent.addHierarchyListener(e -> {
            JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(
                    JScrollPane.class, textComponent);
            if (sp != null) {
                sp.getViewport().addChangeListener(evt -> repaint());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(textComponent.getFont());

        FontMetrics fm = g2d.getFontMetrics();

        JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(
                JScrollPane.class, textComponent);
        int scrollY = 0;
        if (sp != null) {
            scrollY = sp.getViewport().getViewPosition().y;
        }

        Element root = textComponent.getDocument().getDefaultRootElement();
        int lineCount = root.getElementCount();

        for (int line = 0; line < lineCount; line++) {
            try {
                Element lineElement = root.getElement(line);
                int lineStart = lineElement.getStartOffset();

                Rectangle rect = textComponent.modelToView2D(lineStart).getBounds();
                int y = rect.y - scrollY + fm.getAscent();

                if (y < -fm.getHeight()) continue;
                if (y > getHeight() + fm.getHeight()) break;

                String lineNumber = String.valueOf(line + 1);
                int x = getWidth() - fm.stringWidth(lineNumber) - 5;
                g2d.drawString(lineNumber, x, y);

            } catch (BadLocationException ex) {
                // ignorar
            }
        }
    }
}
