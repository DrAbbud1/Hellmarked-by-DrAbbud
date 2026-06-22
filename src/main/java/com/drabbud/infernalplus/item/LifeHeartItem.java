package com.drabbud.infernalplus.item;

import com.drabbud.infernalplus.InfernalPlus;
import com.drabbud.infernalplus.attachment.IPAttachments;
import com.drabbud.infernalplus.config.IPConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Corazón infernal. Al usarlo (clic derecho) sube la vida máxima permanente del jugador
 * en 2 HP (1 corazón), hasta un tope configurable. Se consume al usarlo. El bonus se guarda
 * como AttributeModifier permanente con un ID fijo (idempotente, no se duplica al recargar).
 */
public class LifeHeartItem extends Item {

    private static final ResourceLocation HEALTH_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(InfernalPlus.MODID, "life_heart_bonus");

    public LifeHeartItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        int consumed = player.getData(IPAttachments.LIFE_HEARTS.get());
        int max = IPConfig.LIFE_HEART_MAX.get();

        if (consumed >= max) {
            if (level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("item.infernalplus.life_heart.max")
                                .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            int newCount = consumed + 1;
            player.setData(IPAttachments.LIFE_HEARTS.get(), newCount);
            applyHealthBonus(player, newCount);
            // curar el corazón recién ganado
            player.heal(2.0f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.2f);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** Aplica (o reemplaza) el modificador de vida según cuántos corazones lleve el jugador. */
    public static void applyHealthBonus(Player player, int hearts) {
        AttributeInstance inst = player.getAttribute(Attributes.MAX_HEALTH);
        if (inst == null) return;
        inst.removeModifier(HEALTH_BONUS_ID);
        if (hearts > 0) {
            inst.addPermanentModifier(new AttributeModifier(
                    HEALTH_BONUS_ID, hearts * 2.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
