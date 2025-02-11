package wile.redstonepen.blocks.circuitComponents;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wile.redstonepen.ModContent;
import wile.redstonepen.libmc.Utils;
import wile.redstonepen.libmc.Overlay;
import wile.redstonepen.libmc.RsSignals;
import wile.redstonepen.libmc.StandardBlocks;
import javax.annotation.Nullable;
import java.util.*;
import static zipCoder.redstonetweaks.RedstoneTweaks.MOD_LOGGER;

public class PulseRelayBlock extends CircuitComponents.RelayBlock {
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