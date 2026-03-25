package com.unillanos.editor.core;

import com.unillanos.editor.core.exception.PluginLoadException;
import com.unillanos.editor.core.ioc.IoCContainer;
import com.unillanos.plugin.PluginDescriptor;
import java.io.File;
import java.util.List;

public class Microkernel {

    private final IoCContainer containerIoC;
    private final MessageBus messageBus;

    public Microkernel() {
        this.containerIoC = new IoCContainer();
        this.messageBus = new MessageBus();
    }

    public PluginDescriptor loadPlugin(File file) throws PluginLoadException {
        PluginDescriptor descriptor = containerIoC.loadPlugin(file);
        messageBus.publish("Componente cargado: " + descriptor.getPluginName());
        return descriptor;
    }

    public void activatePlugin(PluginDescriptor descriptor) throws PluginLoadException {
        containerIoC.activatePlugin(descriptor);
        messageBus.publish("Componente activo: " + descriptor.getPluginName());
    }

    /**
     * Ejecuta el plugin activo sobre el texto proporcionado.
     *
     * @param inputText texto del área "Archivo Inicial"
     * @return texto procesado para mostrar en "Archivo procesado"
     */
    public String executeActivePlugin(String inputText) throws PluginLoadException {
        String result = containerIoC.executeActive(inputText);
        messageBus.publish("Ejecución completada — plugin: " + containerIoC.getActiveDescriptor().getPluginName());
        return result;
    }

    public List<PluginDescriptor> getLoadedPlugins() {
        return containerIoC.getRegisteredDescriptors();
    }

    public PluginDescriptor getActiveDescriptor() {
        return containerIoC.getActiveDescriptor();
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }
}
