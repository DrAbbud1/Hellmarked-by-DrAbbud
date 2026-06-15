package com.drabbud.infernalplus.modifier;

import com.drabbud.infernalplus.InfernalPlus;
import com.drabbud.infernalplus.modifier.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class IPModifiers {

    public static final ResourceKey<Registry<IModifier>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(InfernalPlus.MODID, "modifiers"));

    public static final Registry<IModifier> REGISTRY =
            new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();

    public static final DeferredRegister<IModifier> MODIFIERS =
            DeferredRegister.create(REGISTRY_KEY, InfernalPlus.MODID);

    // --- Registro de cada modificador del set "Infernal Mobs" ---
    public static final Supplier<IModifier> TOUGH      = reg(new ToughModifier());
    public static final Supplier<IModifier> SPRINT     = reg(new SprintModifier());
    public static final Supplier<IModifier> RUST       = reg(new RustModifier());
    public static final Supplier<IModifier> FIERY      = reg(new FieryModifier());
    public static final Supplier<IModifier> POISONOUS  = reg(new PoisonousModifier());
    public static final Supplier<IModifier> WITHERING  = reg(new WitheringModifier());
    public static final Supplier<IModifier> WEAKNESS   = reg(new WeaknessModifier());
    public static final Supplier<IModifier> SLOWNESS   = reg(new SlownessModifier());
    public static final Supplier<IModifier> BLINDING   = reg(new BlindingModifier());
    public static final Supplier<IModifier> HUNGER     = reg(new HungerModifier());
    public static final Supplier<IModifier> THIEF      = reg(new ThiefModifier());
    public static final Supplier<IModifier> KNOCKBACK  = reg(new KnockbackModifier());
    public static final Supplier<IModifier> VENGEANCE  = reg(new VengeanceModifier());
    public static final Supplier<IModifier> REGEN      = reg(new RegenModifier());
    public static final Supplier<IModifier> LIFESTEAL  = reg(new LifestealModifier());
    public static final Supplier<IModifier> EXPLOSIVE  = reg(new ExplosiveModifier());
    public static final Supplier<IModifier> GHASTLY    = reg(new GhastlyModifier());
    public static final Supplier<IModifier> WEBBER     = reg(new WebberModifier());
    public static final Supplier<IModifier> BULWARK    = reg(new BulwarkModifier());
    public static final Supplier<IModifier> SAPPER     = reg(new SapperModifier());
    public static final Supplier<IModifier> GIANT      = reg(new GiantModifier());
    public static final Supplier<IModifier> SWIFT      = reg(new SwiftModifier());
    public static final Supplier<IModifier> GRIEFER    = reg(new GrieferModifier());
    public static final Supplier<IModifier> KAMIKAZE   = reg(new KamikazeModifier());
    public static final Supplier<IModifier> STALKER    = reg(new StalkerModifier());

    private static Supplier<IModifier> reg(IModifier instance) {
        return MODIFIERS.register(instance.id(), () -> instance);
    }

    private IPModifiers() {}
}
