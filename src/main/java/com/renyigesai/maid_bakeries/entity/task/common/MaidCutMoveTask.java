package com.renyigesai.maid_bakeries.entity.task.common;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MaidCutMoveTask extends MaidMoveToBlockTask {

    private final BoundingBox checkRange;

    public MaidCutMoveTask(float movementSpeed, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
        this.setMaxCheckRate(45);
        this.checkRange = new BoundingBox(-1, -1, -1, 1, 2, 1);
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel serverLevel, EntityMaid entityMaid, BlockPos pos) {
        /*获取方块是否拥有PILE状态,如果拥有需在1的时候启动*/
        BlockState blockState = serverLevel.getBlockState(pos);
        boolean flag;
        int i = blockState.getBlock().getStateDefinition().getProperty("pile") instanceof IntegerProperty i2 ? blockState.getValue(i2) : -1;
        if (i == -1){
            flag = true;
        }else {
            flag = i != 2;
        }
        return (blockState.getBlock() instanceof com.renyigesai.bakeries.api.blocks.AKnifeCutBlock && flag);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long l) {
        searchForDestination(level, maid);
    }

    @Override
    protected boolean checkPathReach(EntityMaid maid, MaidPathFindingBFS pathFinding, BlockPos pos) {
        for (int x = checkRange.minX(); x <= checkRange.maxX(); x++) {
            for (int y = checkRange.minY(); y <= checkRange.maxY(); y++) {
                for (int z = checkRange.minZ(); z <= checkRange.maxZ(); z++) {
                    if (pathFinding.canPathReach(pos.offset(x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
