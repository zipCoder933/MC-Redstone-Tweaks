package wile.redstonepen.blocks.circuitComponents.relay;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import wile.redstonepen.blocks.circuitComponents.CircuitComponents;

import javax.annotation.Nullable;

public class PulseRelayBlock extends RelayBlock {
    public PulseRelayBlock(long config, Properties builder, AABB aabb) {
        super(config, builder, aabb);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction redstone_side) {
        return ((state.getValue(STATE) == 0) || (redstone_side != getOutputFacing(state).getOpposite())) ? 0 : 15;
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rnd) {
        if (state.getValue(STATE) == 0) return;
        state = state.setValue(STATE, 0);
        world.setBlock(pos, state, 2 | 16);
        notifyOutputNeighbourOfStateChange(state, world, pos);
    }

    @Override
    public BlockState update(BlockState state, Level world, BlockPos pos, @Nullable BlockPos fromPos) {
        final boolean powered = isPowered(state, world, pos);
        if (powered != state.getValue(POWERED)) {
            state = state.setValue(POWERED, powered);
            if (powered) {
                boolean trig = (state.getValue(STATE) == 0);
                state = state.setValue(STATE, 1);
                world.setBlock(pos, state, 2 | 16);
                if (trig) notifyOutputNeighbourOfStateChange(state, world, pos);
            } else {
                world.setBlock(pos, state, 2 | 16);
            }
        }
        if (!world.getBlockTicks().hasScheduledTick(pos, this)) {
            world.scheduleTick(pos, this, 2);
        }
        return state;
    }
}