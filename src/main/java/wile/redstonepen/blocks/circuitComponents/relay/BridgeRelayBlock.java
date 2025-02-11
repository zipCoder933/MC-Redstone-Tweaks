package wile.redstonepen.blocks.circuitComponents.relay;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import wile.redstonepen.ModContent;
import wile.redstonepen.blocks.RedstoneTrack;
import wile.redstonepen.blocks.circuitComponents.CircuitComponents;
import wile.redstonepen.libmc.RsSignals;

import javax.annotation.Nullable;

public class BridgeRelayBlock extends RelayBlock {
    private int power_update_recursion_level_ = 0;

    public BridgeRelayBlock(long config, Properties builder, AABB aabb) {
        super(config, builder, aabb);
    }

    protected int getInputPower(Level world, BlockPos relay_pos, Direction redstone_side) {
        final BlockPos pos = relay_pos.relative(redstone_side);
        final BlockState state = world.getBlockState(pos);
        int p = 0;
        if (power_update_recursion_level_ < 32) {
            ++power_update_recursion_level_;
            if (state.is(Blocks.REDSTONE_WIRE)) {
                p = Math.max(0, state.getDirectSignal(world, pos, redstone_side) - 2);
            } else if (state.is(ModContent.references.TRACK_BLOCK)) {
                p = Math.max(0, RedstoneTrack.RedstoneTrackBlock.tile(world, pos).map(te -> te.getRedstonePower(redstone_side, true)).orElse(0) - 2);
            } else if (state.is(ModContent.references.BRIDGE_RELAY_BLOCK)) {
                if (state.getValue(FACING) != world.getBlockState(relay_pos).getValue(FACING)) {
                    p = 0;
                } else if ((state.getValue(ROTATION) & 0x1) != (world.getBlockState(relay_pos).getValue(ROTATION) & 0x1)) {
                    p = 0;
                } else {
                    p = getInputPower(world, pos, redstone_side);
                }
            } else {
                p = state.getSignal(world, pos, redstone_side);
                if ((p < 15) && (!state.isSignalSource()) && (RsSignals.canEmitWeakPower(state, world, pos, redstone_side))) {
                    for (Direction d : Direction.values()) {
                        if (d == redstone_side.getOpposite()) continue;
                        p = Math.max(p, world.getBlockState(pos.relative(d)).getDirectSignal(world, pos.relative(d), d));
                        if (p >= 15) break;
                    }
                }
            }
            if ((--power_update_recursion_level_) < 0) power_update_recursion_level_ = 0;
        }
        return p;
    }

    protected boolean isWireConnected(Level world, BlockPos relay_pos, Direction side) {
        final BlockPos pos = relay_pos.relative(side);
        final BlockState state = world.getBlockState(pos);
        return state.is(Blocks.REDSTONE_WIRE) || state.is(ModContent.references.TRACK_BLOCK);
    }

    protected boolean isSidePowered(Level world, BlockPos pos, Direction side) {
        return world.getSignal(pos.relative(side), side) > 0;
    }

    protected boolean isPowered(BlockState state, Level world, BlockPos pos) {
        return isSidePowered(world, pos, state.getValue(FACING)) || isSidePowered(world, pos, getOutputFacing(state).getOpposite());
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction redstone_side) {
        if ((redstone_side == getOutputFacing(state).getOpposite())) return state.getValue(POWERED) ? 15 : 0;
        if (!(world instanceof ServerLevel sworld)) return 0;
        final Direction left = getLeftFacing(state);
        final Direction right = getRightFacing(state);
        if ((redstone_side != left) && (redstone_side != right)) return 0;
        if (isWireConnected(sworld, pos, redstone_side)) {
            return getInputPower(sworld, pos, redstone_side);
        } else if (world.getBlockState(pos.relative(redstone_side)).isSignalSource()) {
            return getInputPower(sworld, pos, left);
        } else {
            return 0;
        }
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction redstone_side) {
        return getSignal(state, world, pos, redstone_side);
    }

    @Override
    public BlockState update(BlockState state, Level world, BlockPos pos, @Nullable BlockPos fromPos) {
        // Relay branch update
        final boolean powered = isPowered(state, world, pos);
        if (powered != state.getValue(POWERED)) {
            if (!world.getBlockTicks().hasScheduledTick(pos, this)) {
                if (powered) {
                    world.setBlock(pos, (state = state.setValue(POWERED, true)), 2 | 16);
                    world.neighborChanged(pos.relative(getOutputFacing(state)), this, pos);
                } else {
                    world.scheduleTick(pos, this, 2);
                }
            }
            return state;
        }
        if (fromPos != null) {
            // Wire branch update
            final Vec3i v = pos.subtract(fromPos);
            final Direction redstone_side = Direction.getNearest(v.getX(), v.getY(), v.getZ());
            final Direction left = getLeftFacing(state);
            final Direction right = getRightFacing(state);
            if ((redstone_side != left) && (redstone_side != right)) return state;
            power_update_recursion_level_ = 0;
            final BlockPos npos = pos.relative(redstone_side);
            world.getBlockState(npos).neighborChanged(world, npos, this, pos, false);
            final int pr = getInputPower(world, pos, right);
            final int pl = getInputPower(world, pos, left);
            final boolean track_powered = (pr > 0) || (pl > 0);
            if (track_powered != (state.getValue(STATE) == 1))
                world.setBlock(pos, (state = state.setValue(STATE, track_powered ? 1 : 0)), 2 | 16);
        }
        return state;
    }
}