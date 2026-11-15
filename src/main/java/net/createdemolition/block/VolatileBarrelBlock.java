package net.createdemolition.block;

import net.createdemolition.blast.DemolitionBlast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Uçucu Fıçı: darbeye, ısıya, kızıltaşa ve yakındaki patlamalara karşı son derece hassas.
 * İpeksi Dokunuş olmadan kırmaya çalışmak büyük risktir.
 */
public class VolatileBarrelBlock extends Block {
    public static final float POWER = 5.0f;

    public VolatileBarrelBlock(Properties props) { super(props); }

    public void detonate(Level level, BlockPos pos, int delay) {
        if (!(level instanceof ServerLevel sl)) return;
        level.removeBlock(pos, false);
        DemolitionBlast.schedule(sl, pos.getCenter(), POWER, true, delay);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (!old.is(state.getBlock()) && level.hasNeighborSignal(pos)) detonate(level, pos, 1);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        if (level.hasNeighborSignal(pos)) detonate(level, pos, 1);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel sl && !player.isCreative()) {
            Holder<Enchantment> silk = sl.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(Enchantments.SILK_TOUCH);
            boolean careful = EnchantmentHelper.getItemEnchantmentLevel(silk, player.getMainHandItem()) > 0;
            if (!careful && level.random.nextFloat() < 0.6f) {
                DemolitionBlast.schedule(sl, pos.getCenter(), POWER, true, 1);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (level instanceof ServerLevel sl) {
            DemolitionBlast.schedule(sl, pos.getCenter(), POWER, true, 2 + level.random.nextInt(5));
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (fallDistance > 3.0f) detonate(level, pos, 1);
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        detonate(level, hit.getBlockPos(), 1);
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) { return false; }

    // --- Ateş ---
    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction face) { return true; }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) { return 100; }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) { return 15; }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        if (level instanceof ServerLevel sl) DemolitionBlast.schedule(sl, pos.getCenter(), POWER, true, 1);
    }
}
