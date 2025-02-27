package com.firemerald.additionalplacements.block;

import com.firemerald.additionalplacements.block.interfaces.IAdditionalBeaconBeamBlock;
import com.firemerald.additionalplacements.block.interfaces.IStairBlock;
import com.firemerald.additionalplacements.util.VoxelShapes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VerticalStairBlock extends AdditionalPlacementLiquidBlock<StairBlock> implements IStairBlock<StairBlock>
{
	public static final EnumProperty<EnumPlacing> PLACING = EnumProperty.create("placing", EnumPlacing.class);
	public static final EnumProperty<EnumShape> SHAPE = EnumProperty.create("shape", EnumShape.class);
	public static final VoxelShape[][] SHAPE_CACHE = new VoxelShape[4][23];

	static
	{
		for (EnumPlacing placing : EnumPlacing.values())
		{
			VoxelShape[] shapes = SHAPE_CACHE[placing.ordinal()];
			shapes[EnumShape.STRAIGHT.ordinal()] = VoxelShapes.getStraightStairs(placing.clockWiseFront, placing.counterClockWiseFront);
			shapes[EnumShape.INNER_UP.ordinal()] = VoxelShapes.getInnerStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.UP);
			shapes[EnumShape.OUTER_UP_FROM_CW.ordinal()] = shapes[EnumShape.OUTER_UP_FROM_CCW.ordinal()] = shapes[EnumShape.OUTER_UP.ordinal()] = VoxelShapes.getOuterStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.UP);
			shapes[EnumShape.OUTER_FLAT_UP_CW.ordinal()] = shapes[EnumShape.OUTER_FLAT_UP_FROM_CW.ordinal()] = VoxelShapes.getOuterFlatStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.UP);
			shapes[EnumShape.OUTER_FLAT_UP_CCW.ordinal()] = shapes[EnumShape.OUTER_FLAT_UP_FROM_CCW.ordinal()] = VoxelShapes.getOuterFlatStairs(placing.counterClockWiseFront, placing.clockWiseFront, Direction.UP);
			shapes[EnumShape.INNER_DOWN.ordinal()] = VoxelShapes.getInnerStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.DOWN);
			shapes[EnumShape.OUTER_DOWN_FROM_CW.ordinal()] = shapes[EnumShape.OUTER_DOWN_FROM_CCW.ordinal()] = shapes[EnumShape.OUTER_DOWN.ordinal()] = VoxelShapes.getOuterStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.DOWN);
			shapes[EnumShape.OUTER_FLAT_DOWN_CW.ordinal()] = shapes[EnumShape.OUTER_FLAT_DOWN_FROM_CW.ordinal()] = VoxelShapes.getOuterFlatStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.DOWN);
			shapes[EnumShape.OUTER_FLAT_DOWN_CCW.ordinal()] = shapes[EnumShape.OUTER_FLAT_DOWN_FROM_CCW.ordinal()] = VoxelShapes.getOuterFlatStairs(placing.counterClockWiseFront, placing.clockWiseFront, Direction.DOWN);
			shapes[EnumShape.OUTER_TWIST_CW.ordinal()] = VoxelShapes.getOuterTwistStairs(placing.clockWiseFront, placing.counterClockWiseFront, true);
			shapes[EnumShape.OUTER_TWIST_CCW.ordinal()] = VoxelShapes.getOuterTwistStairs(placing.clockWiseFront, placing.counterClockWiseFront, false);
			shapes[EnumShape.OUTER_TWIST_UP_CW.ordinal()]  = VoxelShapes.getOuterTwistStairs(placing.clockWiseFront, Direction.UP, true);
			shapes[EnumShape.OUTER_TWIST_UP_CCW.ordinal()] = VoxelShapes.getOuterTwistStairs(placing.counterClockWiseFront, Direction.UP, false);
			shapes[EnumShape.OUTER_TWIST_DOWN_CW.ordinal()] = VoxelShapes.getOuterTwistStairs(placing.clockWiseFront, Direction.DOWN, true);
			shapes[EnumShape.OUTER_TWIST_DOWN_CCW.ordinal()] = VoxelShapes.getOuterTwistStairs(placing.counterClockWiseFront, Direction.DOWN, false);
		}
	}

	public static VerticalStairBlock of(StairBlock stairs)
	{
		return stairs instanceof BeaconBeamBlock ? new AdditionalBeaconBeamVerticalStairBlock(stairs) : new VerticalStairBlock(stairs);
	}

	private static class AdditionalBeaconBeamVerticalStairBlock extends VerticalStairBlock implements IAdditionalBeaconBeamBlock<StairBlock>
	{
		AdditionalBeaconBeamVerticalStairBlock(StairBlock stairs)
		{
			super(stairs);
		}
	}

	@SuppressWarnings("deprecation")
	private VerticalStairBlock(StairBlock stairs)
	{
		super(stairs);
		this.registerDefaultState(copyProperties(getModelState(), this.stateDefinition.any()).setValue(PLACING, EnumPlacing.NORTH_EAST).setValue(SHAPE, EnumShape.STRAIGHT));
		((IVanillaStairBlock) stairs).setOtherBlock(this);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(PLACING, SHAPE);
		super.createBlockStateDefinition(builder);
	}

	@Override
	@Deprecated
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return SHAPE_CACHE[state.getValue(PLACING).ordinal()][state.getValue(SHAPE).ordinal()];
	}

	@Override
	public BlockState getDefaultVanillaState(BlockState currentState)
	{
		return currentState.is(parentBlock) ? currentState : copyProperties(currentState, parentBlock.defaultBlockState());
	}

	@Override
	public BlockState getDefaultAdditionalState(BlockState currentState)
	{
		return currentState.is(this) ? currentState : copyProperties(currentState, this.defaultBlockState());
	}

	@Override
	public String getTagTypeName()
	{
		return "stair";
	}

	@Override
	public String getTagTypeNamePlural()
	{
		return "stairs";
	}

    public static enum EnumPlacing implements StringRepresentable
    {
    	NORTH_EAST("north_east", Direction.NORTH, Direction.EAST),
    	EAST_SOUTH("east_south", Direction.EAST, Direction.SOUTH),
    	SOUTH_WEST("south_west", Direction.SOUTH, Direction.WEST),
    	WEST_NORTH("west_north", Direction.WEST, Direction.NORTH);

        private final String name;
        public final Direction counterClockWiseFront, clockWiseFront, counterClockWiseBack, clockWiseBack;

        private EnumPlacing(String name, Direction counterClockWise, Direction clockWise)
        {
            this.name = name;
            this.counterClockWiseFront = counterClockWise;
            this.clockWiseFront = clockWise;
            this.counterClockWiseBack = counterClockWise.getOpposite();
            this.clockWiseBack = clockWise.getOpposite();
        }

        @Override
		public String toString()
        {
            return this.name;
        }

        @Override
		public String getSerializedName()
        {
            return this.name;
        }
    }

    public static enum EnumShape implements StringRepresentable
    {
        STRAIGHT("straight", false, false, false, false),

        OUTER_TWIST_CW("outer_twist_clockwise", false, false, false, false),
        OUTER_TWIST_CCW("outer_twist_counter_clockwise", false, false, false, false),


        INNER_UP("inner_up", true, false, false, false),

        OUTER_UP("outer_up", true, false, false, false),
        OUTER_UP_FROM_CW("outer_up_from_clockwise", true, false, true, false),
        OUTER_UP_FROM_CCW("outer_up_from_counter_clockwise", true, false, false, true),

        OUTER_FLAT_UP_CW("outer_flat_up_clockwise", true, false, false, false),
        OUTER_FLAT_UP_FROM_CW("outer_flat_up_from_clockwise", true, false, true, false),

        OUTER_FLAT_UP_CCW("outer_flat_up_counter_clockwise", true, false, false, false),
        OUTER_FLAT_UP_FROM_CCW("outer_flat_up_from_counter_clockwise", true, false, false, true),

        OUTER_TWIST_UP_CW("outer_twist_up_clockwise", true, false, true, false),
        OUTER_TWIST_UP_CCW("outer_twist_up_counter_clockwise", true, false, false, true),


        INNER_DOWN("inner_down", false, true, false, false),

        OUTER_DOWN("outer_down", false, true, false, false),
        OUTER_DOWN_FROM_CW("outer_down_from_clockwise", false, true, true, false),
        OUTER_DOWN_FROM_CCW("outer_down_from_counter_clockwise", false, true, false, true),

        OUTER_FLAT_DOWN_CW("outer_flat_down_clockwise", false, true, false, false),
        OUTER_FLAT_DOWN_FROM_CW("outer_flat_down_from_clockwise", false, true, true, false),

        OUTER_FLAT_DOWN_CCW("outer_flat_down_counter_clockwise", false, true, false, false),
        OUTER_FLAT_DOWN_FROM_CCW("outer_flat_down_from_counter_clockwise", false, true, false, true),

        OUTER_TWIST_DOWN_CW("outer_twist_down_clockwise", false, true, true, false),
        OUTER_TWIST_DOWN_CCW("outer_twist_down_counter_clockwise", false, true, false, true);

        private final String name;
        public final boolean isUp, isDown, isClockwise, isCounterClockwise;

        private EnumShape(String name, boolean isUp, boolean isDown, boolean isClockwise, boolean isCounterClockwise)
        {
            this.name = name;
            this.isUp = isUp;
            this.isDown = isDown;
            this.isClockwise = isClockwise;
            this.isCounterClockwise = isCounterClockwise;
        }

        @Override
		public String toString()
        {
            return this.name;
        }

        @Override
		public String getSerializedName()
        {
            return this.name;
        }
    }
}
