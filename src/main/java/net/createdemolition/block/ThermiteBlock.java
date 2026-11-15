package net.createdemolition.block;

import net.createdemolition.blast.DemolitionBlast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Kor Termiti: patlamaz, yanar. Ateşlendiğinde aşırı sıcak yanarak altındaki blokları eritip aşağı iner.
 * Obsidyen ve daha sert bloklar eritilemez. Suya değerse buhar patlaması olur.
 */
public class ThermiteBlock extends Block {
    public static final BooleanProperty BURNING = BooleanProperty.create("burning");
    public static final IntegerProperty FUEL = IntegerProperty.create("fuel", 0, 15);
    private static final int TICKS_PER_BLOCK = 15;

    public ThermiteBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(BURNING, false).setValue(FUEL, 12));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(BURNING, FUEL); }

    public void ignite(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(BURNING)) return;
        level.setBlock(pos, state.setValue(BURNING, true), 3);
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, 0.6f);
        level.scheduleTick(pos, this, TICKS_PER_BLOCK);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
            ignite(level, pos, state);
            if (stack.is(Items.FLINT_AND_STEEL)) stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            else stack.consume(1, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        if (level.hasNeighborSignal(pos)) ignite(level, pos, state);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BURNING)) return;

        level.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 6, 0.3, 0.1, 0.3, 0.0);
        level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 12, 0.35, 0.3, 0.35, 0.04);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 4, 0.2, 0.4, 0.2, 0.02);
        level.playSound(null, pos, SoundEvents.BLAZE_BURN, SoundSource.BLOCKS, 1.0f, 0.5f + random.nextFloat() * 0.3f);

        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(0.5, 1.0, 0.5))) {
            e.igniteForSeconds(6);
            e.hurt(level.damageSources().inFire(), 5.0f);
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(d);
            if (random.nextFloat() < 0.3f && BaseFireBlock.canBePlacedAt(level, side, d)) {
                level.setBlock(side, BaseFireBlock.getState(level, side), 11);
            }
        }

        int fuel = state.getValue(FUEL);
        BlockPos below = pos.below();
        BlockState bs = level.getBlockState(below);

        if (bs.getFluidState().is(FluidTags.WATER)) { // termit + su = buhar patlaması
            level.removeBlock(pos, false);
            level.setBlock(below, Blocks.AIR.defaultBlockState(), 3);
            DemolitionBlast.detonate(level, below.getCenter(), 2.5f, false, null);
            return;
        }

        float hardness = bs.getDestroySpeed(level, below);
        boolean meltable = below.getY() > level.getMinBuildHeight() && hardness >= 0 && hardness < 50.0f;

        if (fuel > 0 && meltable) {
            if (!bs.isAir()) level.destroyBlock(below, false);
            level.setBlock(below, state.setValue(FUEL, fuel - 1), 3);
            level.scheduleTick(below, this, TICKS_PER_BLOCK);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            if (BaseFireBlock.canBePlacedAt(level, pos, Direction.UP)) level.setBlock(pos, BaseFireBlock.getState(level, pos), 11);
        } else {
            // Yakıt bitti ya da eritilemeyen bir yüzeye ulaştı: geriye cüruf kalır
            level.setBlock(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        }
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction face) { return true; }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) { return 20; }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) { return 5; }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        level.setBlock(pos, state, 3); // ateşin yerine geri koy ve tutuştur
        ignite(level, pos, state);
    }
}
