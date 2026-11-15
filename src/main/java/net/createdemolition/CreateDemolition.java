package net.createdemolition;

import net.createdemolition.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(CreateDemolition.MODID)
public class CreateDemolition {
    public static final String MODID = "createdemolition";

    public CreateDemolition(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModCreativeTab.TABS.register(modBus);
    }
}
