package net.createdemolition.registry;

import net.createdemolition.CreateDemolition;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateDemolition.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.createdemolition"))
                    .icon(() -> new ItemStack(ModItems.DYNAMITE.get()))
                    .displayItems((params, out) -> ModItems.ITEMS.getEntries().forEach(e -> {
                        if (e.get() != ModItems.INCOMPLETE_BLASTING_CAP.get()) out.accept(e.get());
                    }))
                    .build());
}
