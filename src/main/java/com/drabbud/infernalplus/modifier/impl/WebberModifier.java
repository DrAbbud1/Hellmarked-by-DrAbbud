package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** Coloca telarañas bajo el objetivo al golpear (con cooldown). */
public class WebberModifier implements IModifier {
    public String id() { return "webber"; }
    public int weight() { return 7; }
    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        var data = self.getData(com.drabbud.infernalplus.attachment.IPAttachments.INFERNAL.get());
        if (!data.ready("webber")) return;
        data.setCooldown("webber", 100);
        Level level = target.level();
        BlockPos pos = target.blockPosition();
        if (level.getBlockState(pos).isAir()) {
            level.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
        }
    }
}
