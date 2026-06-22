package com.drabbud.infernalplus.item;

import com.drabbud.infernalplus.InfernalPlus;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Cristal infernal. Clic derecho sobre una pieza de equipo para añadirle un bonus permanente.
 *
 * IMPORTANTE: el cristal NO modifica los ATTRIBUTE_MODIFIERS del ítem (eso rompía la armadura
 * base de las piezas vanilla). En su lugar, guarda un contador por-tipo en el componente
 * CRYSTAL_LEVELS del ítem, y el bonus real se aplica AL JUGADOR cuando lleva la pieza equipada
 * (ver CrystalBonusHandler). Así la armadura/daño base del ítem nunca se toca.
 */
public class CrystalItem extends Item {

    public enum Type {
        ARMOR("crystal_armor", Attributes.ARMOR, 1.0,
                AttributeModifier.Operation.ADD_VALUE, true),
        DAMAGE("crystal_damage", Attributes.ATTACK_DAMAGE, 1.0,
                AttributeModifier.Operation.ADD_VALUE, false),
        SPEED("crystal_speed", Attributes.ATTACK_SPEED, 0.2,
                AttributeModifier.Operation.ADD_VALUE, false);

        public final ResourceLocation modId;
        public final Holder<Attribute> attribute;
        public final double perUse;
        public final AttributeModifier.Operation op;
        public final boolean worksOnArmor;

        Type(String id, Holder<Attribute> attr, double perUse,
             AttributeModifier.Operation op, boolean worksOnArmor) {
            this.modId = ResourceLocation.fromNamespaceAndPath(InfernalPlus.MODID, id);
            this.attribute = attr;
            this.perUse = perUse;
            this.op = op;
            this.worksOnArmor = worksOnArmor;
        }
    }

    private final Type type;
    private final Supplier<Integer> maxSupplier;

    public CrystalItem(Properties props, Type type, Supplier<Integer> maxSupplier) {
        super(props);
        this.type = type;
        this.maxSupplier = maxSupplier;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack crystal, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;
        ItemStack target = slot.getItem();
        if (target.isEmpty() || !canApply(target)) return false;

        int stored = getLevel(target, type);
        int max = maxSupplier.get();
        if (stored >= max) return false;

        if (!player.level().isClientSide()) {
            setLevel(target, type, stored + 1);
            if (!player.getAbilities().instabuild) crystal.shrink(1);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.ANVIL_USE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.4f);
        }
        return true;
    }

    private boolean canApply(ItemStack stack) {
        Item item = stack.getItem();
        boolean isArmor = item instanceof ArmorItem;
        boolean isWeapon = item instanceof SwordItem || item instanceof ShieldItem;
        if (type.worksOnArmor) {
            return isArmor || isWeapon;
        }
        return isWeapon;
    }

    // --- helpers estáticos para leer/escribir niveles en cualquier stack ---

    public static int getLevel(ItemStack stack, Type type) {
        Map<String, Integer> data = stack.getOrDefault(IPComponents.CRYSTAL_LEVELS.get(), Map.of());
        return data.getOrDefault(type.name(), 0);
    }

    public static void setLevel(ItemStack stack, Type type, int value) {
        Map<String, Integer> old = stack.getOrDefault(IPComponents.CRYSTAL_LEVELS.get(), Map.of());
        Map<String, Integer> copy = new HashMap<>(old);
        copy.put(type.name(), value);
        stack.set(IPComponents.CRYSTAL_LEVELS.get(), copy);
    }
}
