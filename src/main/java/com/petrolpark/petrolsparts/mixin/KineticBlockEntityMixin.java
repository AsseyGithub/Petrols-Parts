package com.petrolpark.petrolsparts.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.petrolpark.petrolsparts.content.coaxial_gear.CoaxialGearBlock;
import com.petrolpark.petrolsparts.content.coaxial_gear.LongShaftBlock;
import com.petrolpark.petrolsparts.core.block.DirectionalRotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(KineticBlockEntity.class)
public class KineticBlockEntityMixin {
    
    /**
     * Search for connecting Long Shafts.
     * @param block
     * @param state
     * @param neighbours
     */
    @Inject(
        method = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;addPropagationLocations*(Lcom/simibubi/create/content/kinetics/base/IRotate;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/List;)Ljava/util/List;",
        at = @At("HEAD"),
        remap = false
    )
    public void inAddPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours, CallbackInfoReturnable<List<BlockPos>> cir) {
        if (!thisKineticBlockEntity().hasLevel()) return;
        BlockPos pos = thisKineticBlockEntity().getBlockPos();
        Level level = thisKineticBlockEntity().getLevel();
        if (level == null) return;
        for (Direction direction : Direction.values()) {
            BlockState coaxialGearState = level.getBlockState(pos.relative(direction));
            if (CoaxialGearBlock.isCoaxialGear(coaxialGearState) && coaxialGearState.getValue(RotatedPillarKineticBlock.AXIS) == direction.getAxis()) {
                BlockPos longShaftPos = pos.relative(direction, 2);
                BlockState longShaftState = level.getBlockState(longShaftPos);
                if (longShaftState.getBlock() instanceof LongShaftBlock && DirectionalRotatedPillarKineticBlock.getDirection(longShaftState) == direction.getOpposite()) neighbours.add(longShaftPos);
            };
        };
    };

    private KineticBlockEntity thisKineticBlockEntity() {
        return ((KineticBlockEntity)(Object)this);
    };
};
