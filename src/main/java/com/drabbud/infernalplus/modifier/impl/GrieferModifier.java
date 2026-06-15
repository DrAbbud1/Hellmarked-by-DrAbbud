package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.attachment.IPAttachments;
import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rompe bloques en su camino mientras persigue a un objetivo (con cooldown).
 * Respeta la gamerule mobGriefing y nunca rompe bedrock ni bloques de dureza extrema
 * (contenedores, obsidiana, etc.) para evitar destrozos catastróficos.
 */
public class GrieferModifier implements IModifier {
    public String id() { return "griefer"; }
    public int weight() { return 5; }

    public void onTick(LivingEntity e) {
        if (!(e instanceof Mob mob)) return;
        Level level = e.level();
        if (level.isClientSide()) return;
        if (mob.getTarget() == null) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;

        var data = e.getData(IPAttachments.INFERNAL.get());
        if (!data.ready("griefer")) return;

        // bloque justo enfrente, a la altura de la cabeza y de los pies
        BlockPos eyePos = BlockPos.containing(e.getEyePosition().add(e.getLookAngle().scale(0.8)));
        for (BlockPos pos : new BlockPos[]{eyePos, eyePos.below()}) {
            BlockState state = level.getBlockState(pos);
            float hardness = state.getDestroySpeed(level, pos);
            boolean breakable = !state.isAir()
                    && hardness >= 0f && hardness <= 3.0f   // no bedrock (-1), no obsidiana (~50)
                    && state.getBlock() != Blocks.WATER
                    && state.getBlock() != Blocks.LAVA
                    && !state.hasBlockEntity();              // no cofres, hornos, etc.
            if (breakable) {
                level.destroyBlock(pos, false, e);
                data.setCooldown("griefer", 30);
                return;
            }
        }
    }
}
