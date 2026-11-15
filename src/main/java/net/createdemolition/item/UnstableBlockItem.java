package net.createdemolition.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class UnstableBlockItem extends BlockItem implements Unstable {
    private final float volatility;

    public UnstableBlockItem(Block block, Properties props, float volatility) {
        super(block, props);
        this.volatility = volatility;
    }

    @Override public float volatility() { return volatility; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.createdemolition.unstable").withStyle(ChatFormatting.RED));
    }
}
