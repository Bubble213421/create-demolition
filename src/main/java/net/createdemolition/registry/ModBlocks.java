package net.createdemolition.registry;

import net.createdemolition.CreateDemolition;
import net.createdemolition.block.*;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateDemolition.MODID);

    public static final DeferredBlock<VolatileBarrelBlock> VOLATILE_BARREL = BLOCKS.register("volatile_barrel",
            () -> new VolatileBarrelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
                    .strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));

    public static final DeferredBlock<BoomClayChargeBlock> BOOM_CLAY_CHARGE = BLOCKS.register("boom_clay_charge",
            () -> new BoomClayChargeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(0.4f).sound(SoundType.SLIME_BLOCK).noOcclusion().pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<ThermiteBlock> EMBER_THERMITE = BLOCKS.register("ember_thermite",
            () -> new ThermiteBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.6f).sound(SoundType.SAND)
                    .lightLevel(s -> s.getValue(ThermiteBlock.BURNING) ? 15 : 0)));
}
