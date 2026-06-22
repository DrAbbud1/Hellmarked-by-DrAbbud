package com.drabbud.infernalplus.item;

import com.drabbud.infernalplus.InfernalPlus;
import com.drabbud.infernalplus.config.IPConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;

/**
 * Cristal infernal. Clic derecho (sostén el cristal y clic sobre la pieza de equipo en el
 * inventario) para añadirle +1 de armadura permanente, hasta un tope configurable.
 * El bonus se guarda en dos sitios del ítem objetivo:
 *  - ARMOR_BONUS (nuestro componente): cuántos cristales lleva, para el tope.
 *  - ATTRIBUTE_MODIFIERS (vanilla): el +armadura real que el juego aplica al equipar.
 */
public class ArmorCrystalItem extends Item {

    private static final ResourceLocation ARMOR_MOD_ID =
            ResourceLocation.fromNamespaceAndPath(InfernalPlus.MODID, "crystal_armor");

    public ArmorCrystalItem(Properties props) {
        super(props);
    }

    /** Se dispara cuando el jugador clica el cristal (this) sobre otro stack en el inventario. */
    @Override
    public boolean overrideStackedOnOther(ItemStack crystal, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;
        ItemStack target = slot.getItem();
        if (target.isEmpty() || !isEquippable(target)) return false;

        int current = target.getOrDefault(IPComponents.ARMOR_BONUS.get(), 0);
        int max = IPConfig.ARMOR_CRYSTAL_MAX.get();
        if (current >= max) return false;

        if (!player.level().isClientSide()) {
            int next = current + 1;
            target.set(IPComponents.ARMOR_BONUS.get(), next);
            applyArmorModifiers(target, next);
            if (!player.getAbilities().instabuild) {
                crystal.shrink(1);
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.ANVIL_USE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.4f);
        }
        return true; // consumimos la interacción
    }

    /** Reconstruye el componente de modificadores del ítem añadiendo nuestro +armadura. */
    private static void applyArmorModifiers(ItemStack stack, int bonus) {
        // partir de los modificadores por defecto del ítem para no borrar los suyos
        ItemAttributeModifiers base = stack.getItem().getDefaultInstance()
                .get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (base == null) base = ItemAttributeModifiers.EMPTY;

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        // re-añadir los modificadores originales del ítem
        for (ItemAttributeModifiers.Entry e : base.modifiers()) {
            builder.add(e.attribute(), e.modifier(), e.slot());
        }
        // añadir nuestro bonus de armadura (se aplica en cualquier slot de equipo)
        builder.add(Attributes.ARMOR,
                new AttributeModifier(ARMOR_MOD_ID, bonus, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.ANY);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static boolean isEquippable(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ArmorItem || item instanceof SwordItem || item instanceof ShieldItem;
    }
}
