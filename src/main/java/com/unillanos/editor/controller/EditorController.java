package com.unillanos.editor.controller;

import com.unillanos.editor.core.Microkernel;
import com.unillanos.editor.core.exception.PluginLoadException;
import com.unillanos.plugin.PluginDescriptor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EditorController {

    private final Microkernel kernel;

    private Consumer<PluginDescriptor> onPluginLoaded;
    private Consumer<String> onPluginExecuted;
    private Consumer<String> onFileLoaded;
    private BiConsumer<String, String> onError;
    private Consumer<String> onStatusMessage;

    public EditorController(Microkernel kernel) {
        this.kernel = kernel;
        subscribeToMessageBus();
    }

    public void setOnPluginLoaded(Consumer<PluginDescriptor> callback) {
        this.onPluginLoaded = callback;
    }

    public void setOnPluginExecuted(Consumer<String> callback) {
        this.onPluginExecuted = callback;
    }

    public void setOnFileLoaded(Consumer<String> callback) {
        this.onFileLoaded = callback;
    }

    public void setOnError(BiConsumer<String, String> callback) {
        this.onError = callback;
    }

    public void setOnStatusMessage(Consumer<String> callback) {
        this.onStatusMessage = callback;
    }

    //  Acciones invocadas desde la vista

    public void loadPlugin(File file) {
        try {
            PluginDescriptor descriptor = kernel.loadPlugin(file);
            notify(onPluginLoaded, descriptor);
        } catch (PluginLoadException ex) {
            notifyError("Error al cargar componente", ex.getMessage());
        }
    }

    public void executePlugin(PluginDescriptor descriptor, String inputText) {
        if (descriptor == null) {
            notifyError("Sin selección", "Seleccione un componente de la lista.");
            return;
        }
        try {
            kernel.activatePlugin(descriptor);
            String result = kernel.executeActivePlugin(inputText);
            notify(onPluginExecuted, result);
        } catch (PluginLoadException ex) {
            notifyError("Error al ejecutar componente", ex.getMessage());
        }
    }

    public void loadTextFile(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            notify(onFileLoaded, content);
        } catch (IOException ex) {
            notifyError("Error al leer archivo", ex.getMessage());
        }
    }

    public List<PluginDescriptor> getLoadedPlugins() {
        return kernel.getLoadedPlugins();
    }


    //  Bus de mensajes
    private void subscribeToMessageBus() {
        kernel.getMessageBus().addListener(msg -> notify(onStatusMessage, msg));
    }

    private <T> void notify(Consumer<T> callback, T value) {
        if (callback != null) callback.accept(value);
    }

    private void notifyError(String title, String message) {
        if (onError != null) onError.accept(title, message);
    }

}