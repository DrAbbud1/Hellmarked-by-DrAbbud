package com.drabbud.infernalplus.event;

/**
 * Interruptor global en memoria. Cuando está desactivado, no se generan nuevos infernales.
 * No persiste entre reinicios: siempre arranca activado.
 */
public final class InfernalState {

    private static volatile boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    private InfernalState() {}
}
