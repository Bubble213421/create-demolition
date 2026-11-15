package net.createdemolition.item;

import net.createdemolition.block.BoomClayChargeBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Shift + sağ tık: kurulu şarjı bağla/çöz. Sağ tık: bağlı tüm şarjları ateşle. */
public class RemoteDetonatorItem extends DescribedItem {
    public static final int RANGE = 128;
    public static final int MAX_LINKS = 16;

    public RemoteDetonatorItem(Properties props) { super(props); }

    private static List<Long> getLinks(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        List<Long> list = new ArrayList<>();
        for (long l : tag.getLongArray("Links")) list.add(l);
        return list;
    }

    private static void setLinks(ItemStack stack, List<Long> links) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                t -> t.putLongArray("Links", links.stream().mapToLong(Long::longValue).toArray()));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (player == null || !player.isShiftKeyDown() || !(state.getBlock() instanceof BoomClayChargeBlock)) {
            return InteractionResult.PASS;
        }
        if (!ctx.getLevel().isClientSide) {
            ItemStack stack = ctx.getItemInHand();
            List<Long> links = getLinks(stack);
            long key = ctx.getClickedPos().asLong();
            if (links.remove(key)) {
                player.displayClientMessage(Component.translatable("message.createdemolition.unlinked"), true);
            } else if (links.size() >= MAX_LINKS) {
                player.displayClientMessage(Component.translatable("message.createdemolition.too_many", MAX_LINKS)
                        .withStyle(ChatFormatting.RED), true);
            } else {
                links.add(key);
                player.displayClientMessage(Component.translatable("message.createdemolition.linked", links.size()), true);
            }
            setLinks(stack, links);
            ctx.getLevel().playSound(null, ctx.getClickedPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5f, 1.6f);
        }
        return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        level.playSound(null, player.blockPosition(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 1.0f, 0.6f);
        if (level instanceof ServerLevel sl) {
            List<Long> links = getLinks(stack);
            int fired = 0;
            for (long l : links) {
                BlockPos pos = BlockPos.of(l);
                if (!sl.isLoaded(pos) || !pos.closerToCenterThan(player.position(), RANGE)) continue;
                BlockState s = sl.getBlockState(pos);
                if (s.getBlock() instanceof BoomClayChargeBlock charge && s.getValue(BoomClayChargeBlock.ARMED)) {
                    // Sinyal gecikmesi: şarjlar art arda patlar
                    charge.detonate(sl, pos, s, 2 + fired * 2);
                    fired++;
                }
            }
            setLinks(stack, new ArrayList<>());
            player.displayClientMessage(fired > 0
                    ? Component.translatable("message.createdemolition.fired", fired)
                    : Component.translatable("message.createdemolition.no_signal").withStyle(ChatFormatting.GRAY), true);
        }
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.createdemolition.links", getLinks(stack).size(), MAX_LINKS)
                .withStyle(ChatFormatting.YELLOW));
    }
}
