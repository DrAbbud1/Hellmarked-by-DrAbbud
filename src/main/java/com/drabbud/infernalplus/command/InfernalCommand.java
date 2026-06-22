package com.drabbud.infernalplus.command;

import com.drabbud.infernalplus.attachment.IPAttachments;
import com.drabbud.infernalplus.attachment.InfernalData;
import com.drabbud.infernalplus.modifier.IModifier;
import com.drabbud.infernalplus.event.InfernalSelector;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * /infernal spawn <mob> [tier] [count]   -> aparece un mob infernal forzado
 * /infernal clear [radius]               -> elimina mobs hellmarked cercanos
 * /infernal info                         -> muestra modificadores del mob mirado
 *
 * Requiere permiso de operador (nivel 2).
 */
public final class InfernalCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                net.minecraft.commands.CommandBuildContext buildCtx) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("hellmarked")
                .requires(src -> src.hasPermission(2));

        // --- spawn ---
        root.then(Commands.literal("spawn")
                .then(Commands.argument("entity", net.minecraft.commands.arguments.ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggestResource(
                                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.keySet(), builder))
                        .executes(ctx -> spawn(ctx, -1, 1))
                        .then(Commands.argument("tier", IntegerArgumentType.integer(0, 2))
                                .executes(ctx -> spawn(ctx, IntegerArgumentType.getInteger(ctx, "tier"), 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 50))
                                        .executes(ctx -> spawn(ctx,
                                                IntegerArgumentType.getInteger(ctx, "tier"),
                                                IntegerArgumentType.getInteger(ctx, "count")))))));

        // --- clear ---
        root.then(Commands.literal("clear")
                .executes(ctx -> clear(ctx, 32))
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 256))
                        .executes(ctx -> clear(ctx, IntegerArgumentType.getInteger(ctx, "radius")))));

        // --- info ---
        root.then(Commands.literal("info")
                .executes(InfernalCommand::info));

        // --- toggle: on / off / status ---
        root.then(Commands.literal("toggle")
                .then(Commands.literal("on").executes(ctx -> setEnabled(ctx, true)))
                .then(Commands.literal("off").executes(ctx -> setEnabled(ctx, false)))
                .then(Commands.literal("status").executes(InfernalCommand::status)));

        com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> node = dispatcher.register(root);

        // alias: /infernal sigue funcionando, redirige al árbol de /hellmarked
        dispatcher.register(Commands.literal("infernal")
                .requires(src -> src.hasPermission(2))
                .redirect(node));
    }

    private static int setEnabled(CommandContext<CommandSourceStack> ctx, boolean on) {
        CommandSourceStack src = ctx.getSource();
        com.drabbud.infernalplus.event.InfernalState.setEnabled(on);

        if (on) {
            src.sendSuccess(() -> Component.literal("Hellmarked ACTIVADO. Los mobs hellmarked volverán a aparecer."), true);
            return 1;
        }

        // al desactivar: pausa spawns + limpia todos los mobs hellmarked de todas las dimensiones cargadas
        int removed = 0;
        for (ServerLevel level : src.getServer().getAllLevels()) {
            List<LivingEntity> toRemove = new ArrayList<>();
            for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
                if (e instanceof LivingEntity le
                        && le.getData(IPAttachments.INFERNAL.get()).isInfernal()) {
                    toRemove.add(le);
                }
            }
            for (LivingEntity le : toRemove) le.discard();
            removed += toRemove.size();
        }
        int finalRemoved = removed;
        src.sendSuccess(() -> Component.literal(
                "Hellmarked DESACTIVADO. Spawns pausados y " + finalRemoved + " hellmarked eliminados."), true);
        return removed;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        boolean on = com.drabbud.infernalplus.event.InfernalState.isEnabled();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Hellmarked está " + (on ? "ACTIVADO" : "DESACTIVADO") + "."), false);
        return 1;
    }

    private static int spawn(CommandContext<CommandSourceStack> ctx, int tier, int count) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        Vec3 pos = src.getPosition();

        ResourceLocation id = net.minecraft.commands.arguments.ResourceLocationArgument.getId(ctx, "entity");
        var opt = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(id);
        if (opt.isEmpty()) {
            src.sendFailure(Component.literal("Entidad desconocida: " + id));
            return 0;
        }
        EntityType<?> type = opt.get();

        int spawned = 0;
        for (int i = 0; i < count; i++) {
            Entity e = type.spawn(level, net.minecraft.core.BlockPos.containing(pos), MobSpawnType.COMMAND);
            if (!(e instanceof LivingEntity living)) {
                src.sendFailure(Component.literal("Esa entidad no es un ser vivo."));
                if (e != null) e.discard();
                return 0;
            }
            int useTier = tier >= 0 ? tier : rollTier(level);
            int modCount = baseModCount(useTier, level);
            List<IModifier> mods = InfernalSelector.pickRandom(modCount, level.getRandom());
            InfernalSelector.applyInfernal(living, mods, useTier, 1.0);
            spawned++;
        }

        int finalSpawned = spawned;
        src.sendSuccess(() -> Component.literal("Aparecieron " + finalSpawned + " hellmarked."), true);
        return spawned;
    }

    private static int clear(CommandContext<CommandSourceStack> ctx, int radius) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        Vec3 pos = src.getPosition();
        AABB box = new AABB(pos, pos).inflate(radius);

        List<LivingEntity> toRemove = new ArrayList<>();
        for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, box)) {
            InfernalData d = le.getData(IPAttachments.INFERNAL.get());
            if (d.isInfernal()) toRemove.add(le);
        }
        for (LivingEntity le : toRemove) le.discard();

        int n = toRemove.size();
        src.sendSuccess(() -> Component.literal("Eliminados " + n + " hellmarked en radio " + radius + "."), true);
        return n;
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        Entity viewer = src.getEntity();
        if (!(viewer instanceof LivingEntity looker)) {
            src.sendFailure(Component.literal("Debes ser una entidad para usar esto."));
            return 0;
        }

        LivingEntity target = raycastEntity(looker, 32.0);
        if (target == null) {
            src.sendFailure(Component.literal("No apuntas a ninguna entidad."));
            return 0;
        }
        InfernalData d = target.getData(IPAttachments.INFERNAL.get());
        if (!d.isInfernal()) {
            src.sendSuccess(() -> Component.literal("Esa entidad no está hellmarked."), false);
            return 0;
        }

        String tierName = switch (d.tier()) {
            case 2 -> "Infernal";
            case 1 -> "Ultra";
            default -> "Elite";
        };
        StringBuilder sb = new StringBuilder("Tier " + tierName + " | mods: ");
        List<ResourceLocation> ids = d.ids();
        for (int i = 0; i < ids.size(); i++) {
            sb.append(ids.get(i).getPath());
            if (i < ids.size() - 1) sb.append(", ");
        }
        sb.append(" | vida: ").append(String.format("%.0f/%.0f", target.getHealth(), target.getMaxHealth()));
        String msg = sb.toString();
        src.sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }

    // --- helpers ---

    private static int rollTier(ServerLevel level) {
        var rng = level.getRandom();
        if (rng.nextInt(Math.max(1, com.drabbud.infernalplus.config.IPConfig.ULTRA_RARITY.get())) == 0) {
            if (rng.nextInt(Math.max(1, com.drabbud.infernalplus.config.IPConfig.INFERNAL_RARITY.get())) == 0) {
                return 2;
            }
            return 1;
        }
        return 0;
    }

    private static int baseModCount(int tier, ServerLevel level) {
        var rng = level.getRandom();
        int min = com.drabbud.infernalplus.config.IPConfig.MIN_MODIFIERS.get();
        int max = Math.max(min, com.drabbud.infernalplus.config.IPConfig.MAX_MODIFIERS.get());
        int count = min + rng.nextInt(max - min + 1);
        if (tier >= 1) count += 3 + rng.nextInt(2);
        if (tier >= 2) count += 3 + rng.nextInt(2);
        return count;
    }

    /** Raycast simple para encontrar la entidad viva a la que apunta 'looker'. */
    private static LivingEntity raycastEntity(LivingEntity looker, double range) {
        Vec3 eye = looker.getEyePosition();
        Vec3 dir = looker.getLookAngle();
        Vec3 end = eye.add(dir.scale(range));
        AABB search = looker.getBoundingBox().expandTowards(dir.scale(range)).inflate(1.0);

        LivingEntity best = null;
        double bestDist = range * range;
        for (Entity e : looker.level().getEntities(looker, search)) {
            if (!(e instanceof LivingEntity le)) continue;
            AABB hit = le.getBoundingBox().inflate(0.3);
            var clip = hit.clip(eye, end);
            if (clip.isPresent()) {
                double dist = eye.distanceToSqr(clip.get());
                if (dist < bestDist) {
                    bestDist = dist;
                    best = le;
                }
            }
        }
        return best;
    }

    private InfernalCommand() {}
}
