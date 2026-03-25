package com.unillanos.editor;

import com.unillanos.editor.controller.EditorController;
import com.unillanos.editor.core.Microkernel;
import com.unillanos.editor.vista.EditorFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        Microkernel kernel = new Microkernel();
        EditorController controller = new EditorController(kernel);

        SwingUtilities.invokeLater(() -> {
            EditorFrame frame = new EditorFrame(controller);
            frame.setVisible(true);
        });
    }
}
