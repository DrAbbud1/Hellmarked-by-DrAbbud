package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.attachment.IPAttachments;
import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * El mob es siempre invisible. Solo se revela durante 1 segundo (20 ticks) cuando ataca
 * o cuando recibe daño del jugador; pasado ese segundo vuelve a desaparecer.
 * La proximidad NO lo revela: un stalker quieto a tu lado sigue invisible.
 */
public class StalkerModifier implements IModifier {
    public String id() { return "stalker"; }
    public int weight() { return 6; }
    public String[] incompatibleWith() { return new String[]{"kamikaze"}; }

    private static final int REVEAL_TICKS = 20;       // 1 segundo visible

    public void onApply(LivingEntity e) {
        applyInvisible(e);
    }

    public void onTick(LivingEntity e) {
        Level level = e.level();
        if (level.isClientSide()) return;

        var data = e.getData(IPAttachments.INFERNAL.get());
        int reveal = data.getCounter("stalker_reveal");

        if (reveal > 0) {
            // visible: quitar invisibilidad y descontar el segundo de revelado
            if (e.hasEffect(MobEffects.INVISIBILITY)) {
                e.removeEffect(MobEffects.INVISIBILITY);
            }
            data.setCounter("stalker_reveal", reveal - 1);
        } else {
            // de vuelta a las sombras
            applyInvisible(e);
        }
    }

    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        reveal(self);
    }

    public void onIncomingDamage(LivingEntity self, LivingIncomingDamageEvent event) {
        reveal(self);
    }

    private void reveal(LivingEntity e) {
        e.getData(IPAttachments.INFERNAL.get()).setCounter("stalker_reveal", REVEAL_TICKS);
        if (e.hasEffect(MobEffects.INVISIBILITY)) e.removeEffect(MobEffects.INVISIBILITY);
    }

    private void applyInvisible(LivingEntity e) {
        // efecto largo y renovable; ambient + sin partículas ni icono
        e.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, true, false, false));
    }
}
