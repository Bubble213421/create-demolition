package net.createdemolition.registry;

import net.createdemolition.CreateDemolition;
import net.createdemolition.item.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateDemolition.MODID);

    // --- Kimyasallar ---
    public static final DeferredItem<Item> BLAZE_SALT = simple("blaze_salt");
    public static final DeferredItem<Item> CREEPER_ESSENCE = simple("creeper_essence");
    public static final DeferredItem<Item> SLIME_GEL = simple("slime_gel");
    public static final DeferredItem<Item> VOLATILE_EXTRACT = ITEMS.register("volatile_extract",
            () -> new UnstableItem(new Item.Properties().stacksTo(16), 1.2f));
    public static final DeferredItem<Item> BOOM_CLAY = ITEMS.register("boom_clay",
            () -> new DescribedItem(new Item.Properties()));
    public static final DeferredItem<Item> THERMITE_DUST = simple("thermite_dust");

    // --- Parçalar ---
    public static final DeferredItem<Item> INCOMPLETE_BLASTING_CAP = simple("incomplete_blasting_cap");
    public static final DeferredItem<Item> BLASTING_CAP = ITEMS.register("blasting_cap",
            () -> new DescribedItem(new Item.Properties()));

    // --- Patlayıcılar ve aletler ---
    public static final DeferredItem<Item> DYNAMITE = ITEMS.register("dynamite",
            () -> new DynamiteItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> REMOTE_DETONATOR = ITEMS.register("remote_detonator",
            () -> new RemoteDetonatorItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<BlockItem> VOLATILE_BARREL = ITEMS.register("volatile_barrel",
            () -> new UnstableBlockItem(ModBlocks.VOLATILE_BARREL.get(), new Item.Properties().stacksTo(4), 3.0f));
    public static final DeferredItem<BlockItem> BOOM_CLAY_CHARGE = ITEMS.registerSimpleBlockItem(ModBlocks.BOOM_CLAY_CHARGE);
    public static final DeferredItem<BlockItem> EMBER_THERMITE = ITEMS.registerSimpleBlockItem(ModBlocks.EMBER_THERMITE);

    private static DeferredItem<Item> simple(String name) {
        return ITEMS.registerSimpleItem(name);
    }
}
