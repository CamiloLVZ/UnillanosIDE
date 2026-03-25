package com.unillanos.editor.core.exception;

public class PluginLoadException extends Exception {

    public PluginLoadException(String message) {
        super(message);
    }

    public PluginLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
