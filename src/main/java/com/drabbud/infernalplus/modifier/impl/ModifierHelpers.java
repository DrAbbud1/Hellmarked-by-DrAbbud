package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.InfernalPlus;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class ModifierHelpers {

    public static void addAttribute(LivingEntity e, Holder<Attribute> attr, String name,
                                    double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = e.getAttribute(attr);
        if (inst == null) return;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(InfernalPlus.MODID, name);
        if (inst.getModifier(id) != null) return;
        inst.addPermanentModifier(new AttributeModifier(id, amount, op));
    }

    private ModifierHelpers() {}
}
