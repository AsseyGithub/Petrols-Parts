package com.petrolpark.petrolsparts.content.double_cardan_shaft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.petrolpark.petrolsparts.PetrolsPartsBlockEntityTypes;
import com.petrolpark.petrolsparts.PetrolsPartsBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DoubleCardanShaftBlock extends DirectionalAxisKineticBlock implements IBE<DoubleCardanShaftBlockEntity>{

    public DoubleCardanShaftBlock(Properties properties) {
        super(properties);
    };

    @Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction direction1 = context.getClickedFace().getOpposite();
        Direction direction2;
        if (direction1.getAxis() == Axis.Y) {
            direction2 = context.getHorizontalDirection();
        } else {
            direction2 = context.getNearestLookingVerticalDirection();
        };
        return getBlockstateConnectingDirections(direction1, direction2);
	};

    @Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return Arrays.asList(getDirectionsConnectedByState(state)).contains(face);
	};

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return transform(originalState, new StructureTransform(new BlockPos(0, 0, 0), targetedFace.getAxis(), Rotation.CLOCKWISE_90, Mirror.NONE));
    };

    @Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction xDirection = null, yDirection = null, zDirection = null;
        for (Direction direction : getDirectionsConnectedByState(state)) {
            switch (direction.getAxis()) {
                case X:
                    xDirection = direction;
                    break;
                case Y:
                    yDirection = direction;
                    break;
                case Z:
                    zDirection = direction;
            };
        };
        return new AllShapes.Builder(Block.box(
            xDirection == Direction.WEST ? 0d : 5d,
            yDirection == Direction.DOWN ? 0d : 5d,
            zDirection == Direction.NORTH ? 0d : 5d,
            xDirection == Direction.EAST ? 16d : 11,
            yDirection == Direction.UP ? 16d : 11d,
            zDirection == Direction.SOUTH ? 16d : 11d
        )).build();
	};

    /**
     * XYZXYZ
     * <p>
     * Considering the above list, with the Axis of {@code facing} excluded:<ul>
     * <li>If {@code axisAlongFirst} is true, the shaft connects {@code facing} and the Axis to the right in the same direction.</li>
     * <li>If {@code axisAlongFirst} is false, the shaft connects {@code facing} and the Axis to the left in the opposite direction.</li>
     * </ul></p>
     */
    /*
     * Direction axisAlongFirst Other Direction
     * -Z NORTH  true           -X WEST
     * -Z NORTH  false          +Y UP
     * +Z SOUTH  true           +X EAST
     * +Z SOUTH  false          -Y DOWN
     * +X EAST   true           +Y UP
     * +X EAST   false          -Z NORTH
     * -X WEST   true           -Y DOWN
     * -X WEST   false          +Z SOUTH
     * +Y UP     true           +Z SOUTH
     * +Y UP     false          -X WEST
     * -Y DOWN   true           -Z SOUTH
     * -Y DOWN   false          +Z NORTH
     */
    public static Direction[] getDirectionsConnectedByState(BlockState state) {
        Direction facing = state.getValue(DirectionalKineticBlock.FACING);
        boolean axisAlongFirst = state.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
        Axis secondDirectionAxis;
        switch (facing.getAxis()) {
            case X:
                secondDirectionAxis = axisAlongFirst ? Axis.Y : Axis.Z;
                break;
            case Y:
                secondDirectionAxis = axisAlongFirst ? Axis.Z : Axis.X;
                break;
            case Z:
                secondDirectionAxis = axisAlongFirst ? Axis.X : Axis.Y;
                break;
            default:
                throw new IllegalStateException("Unknown axis");
        };
        return new Direction[]{facing, Direction.fromAxisAndDirection(secondDirectionAxis, facing.getAxisDirection() == AxisDirection.POSITIVE ^ axisAlongFirst ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE)};
    };

    public static BlockState getBlockstateConnectingDirections(Direction direction1, Direction direction2) {
        boolean axisAlongFirst = (direction1.getAxisDirection() == direction2.getAxisDirection());
        Map<Axis, Direction> directionsForEachAxis = Map.of(direction1.getAxis(), direction1, direction2.getAxis(), direction2);
        List<Axis> axes = new ArrayList<>();
        axes.addAll(List.of(Axis.values()));
        axes.remove(direction1.getAxis());
        axes.remove(direction2.getAxis());
        Axis primaryAxis;
        switch (axes.get(0)) {
            case X:
                primaryAxis = axisAlongFirst ? Axis.Y : Axis.Z;
                break;
            case Y:
                primaryAxis = axisAlongFirst ? Axis.Z : Axis.X;
                break;
            case Z:
                primaryAxis = axisAlongFirst ? Axis.X : Axis.Y;
                break;
            default:
                throw new IllegalStateException("Unknown axis");
        };
        return PetrolsPartsBlocks.DOUBLE_CARDAN_SHAFT.getDefaultState().setValue(DirectionalKineticBlock.FACING, directionsForEachAxis.get(primaryAxis)).setValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE, axisAlongFirst);
    };

    public static boolean isPositiveDirection(Direction direction) {
        return Direction.get(AxisDirection.POSITIVE, direction.getAxis()) == direction;
    };

    @Override
	public BlockState rotate(BlockState state, Rotation rot) {
		Direction[] directions = getDirectionsConnectedByState(state);
        return getBlockstateConnectingDirections(rot.rotate(directions[0]), rot.rotate(directions[1]));
	};

    @Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		Direction[] directions = getDirectionsConnectedByState(state);
        return getBlockstateConnectingDirections(mirror.getRotation(directions[0]).rotate(directions[0]), mirror.getRotation(directions[1]).rotate(directions[1]));
	};

    @Override
	public BlockState transform(BlockState state, StructureTransform transform) {
		Direction[] directions = getDirectionsConnectedByState(state);
        return getBlockstateConnectingDirections(transform.mirrorFacing(transform.rotateFacing(directions[0])), transform.mirrorFacing(transform.rotateFacing(directions[1])));
	};

    @Override
    public Class<DoubleCardanShaftBlockEntity> getBlockEntityClass() {
        return DoubleCardanShaftBlockEntity.class;
    };

    @Override
    public BlockEntityType<? extends DoubleCardanShaftBlockEntity> getBlockEntityType() {
        return PetrolsPartsBlockEntityTypes.DOUBLE_CARDAN_SHAFT.get();
    };
    
};
