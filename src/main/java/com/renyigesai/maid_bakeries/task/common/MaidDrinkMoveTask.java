package com.renyigesai.maid_bakeries.task.common;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.renyigesai.bakeries.block.glass_drink_cup.GlassDrinkCupBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MaidDrinkMoveTask extends MaidMoveToBlockTask {

    private final BoundingBox checkRange;

    public MaidDrinkMoveTask(float movementSpeed, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
        this.setMaxCheckRate(45);
        this.checkRange = new BoundingBox(-1, -1, -1, 1, 2, 1);
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel serverLevel, EntityMaid entityMaid, BlockPos pos) {
        BlockState blockState = serverLevel.getBlockState(pos);
        return blockState.getBlock() instanceof GlassDrinkCupBlock;
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
