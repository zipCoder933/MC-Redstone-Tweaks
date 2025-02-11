package wile.redstonepen.blocks.circuitComponents.relay;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import wile.redstonepen.blocks.circuitComponents.CircuitComponents;
import javax.annotation.Nullable;

public class RelayBlock extends CircuitComponents.DirectedComponentBlock {
    protected boolean isPowered(BlockState state, Level world, BlockPos pos) {
        final Direction output_side = getOutputFacing(state);
        final Direction mount_side = state.getValue(FACING);
        for (Direction side : Direction.values()) {
            if (side == output_side) continue;
            if (side == mount_side.getOpposite()) continue;
            if (world.getSignal(pos.relative(side), side) > 0) return true;
        }
        return false;
    }

    public RelayBlock(long config, Properties builder, AABB aabb) {
        super(config, builder, aabb);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction redstone_side) {
        return ((!state.getValue(POWERED)) || (redstone_side != getOutputFacing(state).getOpposite())) ? 0 : 15;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction redstone_side) {
        return getSignal(state, world, pos, redstone_side);
    }

    /**
     * Called when a tick was scheduled for us
     *
     * @param state
     * @param world
     * @param pos
     * @param rnd
     */
    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rnd) {
        final boolean powered = isPowered(state, world, pos);
        if (powered == state.getValue(POWERED)) return;
        if (!powered) {
            world.setBlock(pos, state.setValue(POWERED, false), 2 | 16);
            notifyOutputNeighbourOfStateChange(state, world, pos);
        }
    }

    @Override
    public BlockState update(BlockState state, Level world, BlockPos pos, @Nullable BlockPos fromPos) {
        /**
         * The block update method is called when a change has occured, we schedule a tick in the future
         */
        final boolean powered = isPowered(state, world, pos);
        if (powered == state.getValue(POWERED)) return state;
        if (world.getBlockTicks().hasScheduledTick(pos, this)) return state;
        if (powered) {
            world.setBlock(pos, state.setValue(POWERED, true), 2 | 16);
            notifyOutputNeighbourOfStateChange(state, world, pos);
        } else {
            world.scheduleTick(pos, this, 2);
        }
        return state;
    }
}