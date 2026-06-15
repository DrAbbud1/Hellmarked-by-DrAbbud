package com.drabbud.infernalplus.awareness;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Estado de "alerta" por jugador. Rastrea cuántos ticks lleva sin moverse de forma
 * significativa y la última posición conocida. Se usa para subir la dificultad cuando
 * el jugador está descuidado (quieto, distraído).
 */
public final class AwarenessData {

    public static final AwarenessData EMPTY = new AwarenessData(0, 0, 0, 0);

    public static final Codec<AwarenessData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("idleTicks").forGetter(d -> d.idleTicks),
            Codec.DOUBLE.fieldOf("lastX").forGetter(d -> d.lastX),
            Codec.DOUBLE.fieldOf("lastY").forGetter(d -> d.lastY),
            Codec.DOUBLE.fieldOf("lastZ").forGetter(d -> d.lastZ)
    ).apply(inst, AwarenessData::new));

    public int idleTicks;
    public double lastX, lastY, lastZ;

    public AwarenessData(int idleTicks, double lastX, double lastY, double lastZ) {
        this.idleTicks = idleTicks;
        this.lastX = lastX;
        this.lastY = lastY;
        this.lastZ = lastZ;
    }
}
