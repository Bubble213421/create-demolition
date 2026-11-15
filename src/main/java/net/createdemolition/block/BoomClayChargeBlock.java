package net.createdemolition.block;

import com.mojang.serialization.MapCodec;
import net.createdemolition.blast.DemolitionBlast;
import net.createdemolition.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Patlayıcı Kil Şarjı: duvara/zemine yapıştırılan kararlı patlayıcı.
 * Ateş ve darbeyle patlamaz; mutlaka Fünye takılıp kızıltaş sinyali ya da uzaktan kumandayla ateşlenmelidir.
 * Patlama merkezi yapıştığı bloğun içine kaydırılır, böylece duvarı "keser".
 */
public class BoomClayChargeBlock extends DirectionalBlock {
    public static final MapCodec<BoomClayChargeBlock> CODEC = simpleCodec(BoomClayChargeBlock::new);
    public static final BooleanProperty ARMED = BooleanProperty.create("armed");
    public static final float POWER = 6.5f;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    static {
        SHAPES.put(Direction.UP, Block.box(3, 0, 3, 13, 3, 13));
        SHAPES.put(Direction.DOWN, Block.box(3, 13, 3, 13, 16, 13));
        SHAPES.put(Direction.NORTH, Block.box(3, 3, 13, 13, 13, 16));
        SHAPES.put(Direction.SOUTH, Block.box(3, 3, 0, 13, 13, 3));
        SHAPES.put(Direction.WEST, Block.box(13, 3, 3, 16, 13, 13));
        SHAPES.put(Direction.EAST, Block.box(0, 3, 3, 3, 13, 13));
    }

    public BoomClayChargeBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ARMED, false));
    }

    @Override protected MapCodec<? extends DirectionalBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, ARMED); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Nullable @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState s = defaultBlockState().setValue(FACING, ctx.getClickedFace());
        return s.canSurvive(ctx.getLevel(), ctx.getClickedPos()) ? s : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction f = state.getValue(FACING);
        BlockPos support = pos.relative(f.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, f);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos npos) {
        if (dir == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, dir, neighbor, level, pos, npos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ModItems.BLASTING_CAP.get()) && !state.getValue(ARMED)) {
            level.setBlock(pos, state.setValue(ARMED, true), 3);
            level.playSound(null, pos, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 1.0f, 1.2f);
            stack.consume(1, player);
            if (level.hasNeighborSignal(pos)) detonate(level, pos, state, 1);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        if (!state.canSurvive(level, pos)) { level.destroyBlock(pos, true); return; }
        if (state.getValue(ARMED) && level.hasNeighborSignal(pos)) detonate(level, pos, state, 1);
    }

    /** Sadece fünye takılıysa patlar; aksi halde patlama kili parçalar ama tetiklemez. */
    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        // Blok durumu bu noktada silinmiş olduğu için kontrol onBlockExploded'da yapılır.
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        boolean armed = state.getValue(ARMED);
        super.onBlockExploded(state, level, pos, explosion);
        if (armed && level instanceof ServerLevel sl) {
            DemolitionBlast.schedule(sl, blastCenter(pos, state), POWER, false, 2 + level.random.nextInt(4));
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) { return false; }

    public void detonate(Level level, BlockPos pos, BlockState state, int delay) {
        if (!(level instanceof ServerLevel sl)) return;
        level.removeBlock(pos, false);
        DemolitionBlast.schedule(sl, blastCenter(pos, state), POWER, false, delay);
    }

    private static Vec3 blastCenter(BlockPos pos, BlockState state) {
        Direction into = state.getValue(FACING).getOpposite();
        return pos.getCenter().add(into.getStepX() * 0.6, into.getStepY() * 0.6, into.getStepZ() * 0.6);
    }
}
