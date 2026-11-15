package net.createdemolition.registry;

import net.createdemolition.CreateDemolition;
import net.createdemolition.entity.DynamiteEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, CreateDemolition.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<DynamiteEntity>> DYNAMITE = ENTITIES.register("dynamite",
            () -> EntityType.Builder.<DynamiteEntity>of(DynamiteEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(8).updateInterval(10).build("dynamite"));
}
