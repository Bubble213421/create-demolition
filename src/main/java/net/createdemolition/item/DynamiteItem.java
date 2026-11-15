package net.createdemolition.item;

import net.createdemolition.entity.DynamiteEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DynamiteItem extends DescribedItem {
    public DynamiteItem(Properties props) { super(props); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (!level.isClientSide) {
            DynamiteEntity d = new DynamiteEntity(level, player);
            d.setItem(stack.copyWithCount(1));
            d.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.0f, 1.0f);
            level.addFreshEntity(d);
        }
        player.getCooldowns().addCooldown(this, 15);
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
