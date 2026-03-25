# Editor de Texto – Unillanos V1.0
### Arquitectura Microkernel + IoC (Inversión de Control)

---

## Estructura del proyecto

```
TextEditorMicrokernel/
├── pom.xml
├── plugins-sample/                  ← Descriptores .properties de los plugins incluidos
│   ├── uppercase-plugin.properties
│   ├── wordcount-plugin.properties
│   ├── reverse-plugin.properties
│   └── cleantext-plugin.properties
└── src/main/java/com/unillanos/editor/
    ├── Main.java                    ← Punto de entrada
    ├── core/
    │   ├── Microkernel.java         ← Núcleo de la aplicación
    │   └── MessageBus.java          ← Bus de mensajes (Observer)
    ├── ioc/
    │   ├── IoCContainer.java        ← Contenedor IoC (reflexión + ClassLoader)
    │   └── PluginLoadException.java
    ├── plugin/
    │   ├── TextPlugin.java          ← Interfaz contrato de todos los plugins
    │   └── PluginDescriptor.java    ← Metadatos de un plugin
    ├── plugins/                     ← Plugins incluidos (de ejemplo)
    │   ├── UpperCasePlugin.java
    │   ├── WordCountPlugin.java
    │   ├── ReversePlugin.java
    │   └── CleanTextPlugin.java
    └── ui/
        └── EditorFrame.java         ← Interfaz gráfica Swing
```

---

## Arquitectura

### Microkernel
El `Microkernel` es el núcleo mínimo. **No conoce** ninguna implementación concreta de plugin. Solo provee:
- Registro de plugins
- Bus de mensajes interno
- Delegación al contenedor IoC

### Contenedor IoC
`IoCContainer` aplica **Inversión de Control**:
- Lee el descriptor `.properties` del plugin
- Carga la clase **dinámicamente** con `URLClassLoader`
- Instancia el plugin mediante **reflexión** (`Class.forName` + `newInstance`)
- El núcleo nunca hace `new MiPlugin()` — el contenedor se lo "inyecta"

### Plugins
Cualquier clase que implemente `TextPlugin` puede ser un plugin:

```java
public class MiPlugin implements TextPlugin {
    public String getName()        { return "Mi Plugin"; }
    public String getDescription() { return "Hace algo útil"; }
    public String getVersion()     { return "1.0"; }
    public String execute(String input) { return input.toUpperCase(); }
}
```

---

## Compilar y ejecutar

### Con Maven
```bash
cd TextEditorMicrokernel
mvn package
java -jar target/TextEditorMicrokernel.jar
```

### Con javac manual
```bash
# Desde la raíz del proyecto
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out com.unillanos.editor.Main
```

---

## Cómo usar la aplicación

1. **Cargar archivo de texto inicial**
   - Clic en "Cargar Archivo" (panel derecho, parte superior)
   - Seleccione un `.txt` — el contenido aparece en "Archivo Inicial"

2. **Cargar un componente (plugin)**
   - Clic en "Cargar Archivo" (panel izquierdo, "Cargar componente")
   - Seleccione un archivo `.properties` de la carpeta `plugins-sample/`
   - El plugin aparece en la lista "Componentes cargados"

3. **Ejecutar el componente**
   - Seleccione un plugin de la lista
   - Clic en "Ejecutar componente" (o doble clic en la lista)
   - El resultado aparece en "Archivo procesado"
   - Los mensajes de estado se muestran en "Salida de mensajes"

---

## Crear un plugin externo (en un JAR separado)

1. Cree un proyecto Java con la interfaz `TextPlugin` en el classpath
2. Implemente `TextPlugin` en su clase
3. Incluya en el JAR un archivo `META-INF/plugin.properties`:
   ```properties
   plugin.id          = mi-plugin-externo
   plugin.class       = com.ejemplo.MiPlugin
   plugin.name        = Mi Plugin Externo
   plugin.description = Descripción de lo que hace
   plugin.version     = 1.0
   ```
4. En la aplicación, seleccione el `.jar` en el diálogo de carga
5. El IoC Container lo cargará dinámicamente con su propio ClassLoader

---

## Plugins incluidos

| Plugin             | Descripción                                       |
|--------------------|---------------------------------------------------|
| Mayúsculas         | Convierte todo el texto a MAYÚSCULAS              |
| Contador           | Estadísticas: palabras, líneas, frecuencia        |
| Invertir           | Invierte cada línea del texto                     |
| Limpiar            | Elimina líneas en blanco y espacios extra         |

---

## Diagrama de flujo IoC

```
UI (EditorFrame)
    │  btnCargarComponente clicked
    ▼
Microkernel.loadPlugin(file)
    │
    ▼
IoCContainer.loadPlugin(file)
    │  lee .properties
    │  URLClassLoader.loadClass(className)
    │  Class.newInstance()  ← Reflexión / IoC
    ▼
TextPlugin (instancia concreta, desconocida para el kernel)
    │
    ▼  btnEjecutarComponente clicked
IoCContainer.executeActive(inputText)
    ▼
txtArchivoProcesado.setText(result)
```
