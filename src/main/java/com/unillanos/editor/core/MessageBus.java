package com.unillanos.editor.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Bus de mensajes del Microkernel con historial.
 *
 * Mantiene todos los mensajes publicados y notifica a cada
 * suscriptor con el mensaje nuevo. La vista puede consultar
 * el historial completo para poblar su área de logs.
 */
public class MessageBus {

    public interface Listener {
        void onMessage(String message);
    }

    private final List<Listener> listeners = new ArrayList<Listener>();
    private final List<String>   history   = new ArrayList<String>();

    public void publish(String message) {
        history.add(message);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onMessage(message);
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public List<String> getHistory() {
        return new ArrayList<String>(history);
    }

    public void clearHistory() {
        history.clear();
    }
}