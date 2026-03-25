package com.unillanos.editor.core.ioc;

import com.unillanos.editor.core.exception.PluginLoadException;
import com.unillanos.plugin.PluginDescriptor;
import com.unillanos.plugin.ITextPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Logger;

/**
 * Contenedor IoC (Inversión de Control) del Microkernel.
 * <p>
 * Responsabilidades:
 * 1. Leer archivos .properties que describen plugins.
 * 2. Cargar dinámicamente el .jar o la clase del plugin usando URLClassLoader.
 * 3. Instanciar el plugin (reflexión) e inyectarlo al sistema sin acoplamiento.
 * 4. Mantener el registro de plugins disponibles y activos.
 */
public class IoCContainer {

    private static final Logger logger = Logger.getLogger(IoCContainer.class.getName());

    /**
     * Plugins registrados (descriptor → instancia).
     */
    private final Map<PluginDescriptor, ITextPlugin> pluginsRegistry = new LinkedHashMap<>();

    /**
     * Plugin actualmente seleccionado para ejecución.
     */
    private ITextPlugin activePlugin = null;
    private PluginDescriptor activeDescriptor = null;

    // ------------------------------------------------------------------ //
    //  Carga de plugins                                                   //
    // ------------------------------------------------------------------ //

    /**
     * Carga un plugin a partir de un archivo .jar.
     * <p>
     * Formato esperado del plugin.properties:
     * plugin.id          = miPlugin
     * plugin.class       = com.ejemplo.MiPlugin
     * plugin.name        = Nombre visible
     * plugin.jar         = (opcional) ruta al .jar si la clase no está en el classpath
     *
     * @param file archivo .properties o .jar del plugin
     * @return descriptor del plugin cargado
     * @throws PluginLoadException si algo falla durante la carga
     */
    public PluginDescriptor loadPlugin(File file) throws PluginLoadException {
        if (file == null || !file.exists()) {
            throw new PluginLoadException("El archivo no existe: " + file);
        }

        String name = file.getName().toLowerCase();

        if (name.endsWith(".jar")) {
            return loadFromJar(file);
        } else {
            throw new PluginLoadException("Formato no soportado. Use .jar");
        }
    }

    /**
     * Carga desde un .jar buscando la implementación de TextPlugin.
     */
    private PluginDescriptor loadFromJar(File jarFile) throws PluginLoadException {
        // El .jar debe incluir un META-INF/plugin.properties interno
        try (URLClassLoader tmpLoader = new URLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                Thread.currentThread().getContextClassLoader())) {

            URL metaUrl = tmpLoader.getResource("META-INF/plugin.properties");

            if (metaUrl == null)
                throw new PluginLoadException("El .jar no contiene META-INF/plugin.properties.");

            Properties props = new Properties();
            props.load(metaUrl.openStream());

            String id = required(props, "plugin.id", jarFile);
            String className = required(props, "plugin.class", jarFile);
            String pluginName = props.getProperty("plugin.name", className);

            // Creamos un classloader permanente para este jar
            URLClassLoader permanentLoader = new URLClassLoader(
                    new URL[]{jarFile.toURI().toURL()},
                    Thread.currentThread().getContextClassLoader());

            ITextPlugin instance = instantiate(className, permanentLoader);
            PluginDescriptor descriptor = new PluginDescriptor(id, className, pluginName);
            pluginsRegistry.put(descriptor, instance);
            logger.info("Plugin JAR cargado: " + pluginName);
            return descriptor;

        } catch (IOException e) {
            throw new PluginLoadException("Error leyendo el JAR: " + e.getMessage(), e);
        }
    }

    /**
     * Activa el plugin identificado por su descriptor.
     * El contenedor IoC "inyecta" el plugin activo al núcleo.
     */
    public void activatePlugin(PluginDescriptor descriptor) throws PluginLoadException {
        if (!pluginsRegistry.containsKey(descriptor)) {
            throw new PluginLoadException("Plugin no encontrado en el registro: " + descriptor);
        }
        activeDescriptor = descriptor;
        activePlugin = pluginsRegistry.get(descriptor);
        logger.info("Plugin activado: " + descriptor.getPluginName());
    }

    /**
     * Ejecuta el plugin activo sobre el texto de entrada.
     *
     * @param inputText texto del área "Archivo Inicial"
     * @return texto procesado
     * @throws PluginLoadException si no hay plugin activo
     */
    public String executeActive(String inputText) throws PluginLoadException {
        if (activePlugin == null) {
            throw new PluginLoadException("No hay ningún componente activo. Seleccione y ejecute uno.");
        }
        return activePlugin.execute(inputText);
    }

    // ------------------------------------------------------------------ //
    //  Consultas                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Devuelve todos los descriptores registrados.
     */
    public List<PluginDescriptor> getRegisteredDescriptors() {
        return new ArrayList<>(pluginsRegistry.keySet());
    }

    public PluginDescriptor getActiveDescriptor() {
        return activeDescriptor;
    }

    public ITextPlugin getActivePlugin() {
        return activePlugin;
    }


    private ITextPlugin instantiate(String className, ClassLoader cl) throws PluginLoadException {
        try {
            Class<?> claSs = Class.forName(className, true, cl);
            if (!ITextPlugin.class.isAssignableFrom(claSs)) {
                throw new PluginLoadException("La clase " + className + " no implementa TextPlugin.");
            }
            return (ITextPlugin) claSs.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new PluginLoadException("Clase no encontrada: " + className, e);
        } catch (ReflectiveOperationException e) {
            throw new PluginLoadException("No se pudo instanciar " + className + ": " + e.getMessage(), e);
        }
    }

    private String required(Properties props, String key, File source) throws PluginLoadException {
        String val = props.getProperty(key, "").trim();
        if (val.isEmpty())
            throw new PluginLoadException("Falta la propiedad '" + key + "' en: " + source.getName());

        return val;
    }
}
