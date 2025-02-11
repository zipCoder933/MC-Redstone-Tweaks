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

public class InvertedRelayBlock extends RelayBlock {
    public InvertedRelayBlock(long config, Properties builder, AABB aabb) {
        super(config, builder, aabb);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction redstone_side) {
        return (state.getValue(POWERED) || (redstone_side != getOutputFacing(state).getOpposite())) ? 0 : 15;
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rnd) {
        final boolean powered = isPowered(state, world, pos);
        if (powered == state.getValue(POWERED)) return;
        if (powered) {
            world.setBlock(pos, state.setValue(POWERED, true), 2 | 16);
            notifyOutputNeighbourOfStateChange(state, world, pos);
        }
    }

    @Override
    public BlockState update(BlockState state, Level world, BlockPos pos, @Nullable BlockPos fromPos) {
        final boolean powered = isPowered(state, world, pos);
        if (powered == state.getValue(POWERED)) return state;
        if (world.getBlockTicks().hasScheduledTick(pos, this)) return state;
        if (powered) {
            world.scheduleTick(pos, this, 2);
        } else {
            world.setBlock(pos, state.setValue(POWERED, false), 2 | 16);
            notifyOutputNeighbourOfStateChange(state, world, pos);
        }
        return state;
    }
}