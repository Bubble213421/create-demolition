package net.createdemolition.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class UnstableItem extends DescribedItem implements Unstable {
    private final float volatility;

    public UnstableItem(Properties props, float volatility) {
        super(props);
        this.volatility = volatility;
    }

    @Override public float volatility() { return volatility; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.createdemolition.unstable").withStyle(ChatFormatting.RED));
    }
}
