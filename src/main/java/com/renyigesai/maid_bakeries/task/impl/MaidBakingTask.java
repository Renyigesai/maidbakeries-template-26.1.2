package com.renyigesai.maid_bakeries.task.impl;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import com.renyigesai.maid_bakeries.data.BakingTasks;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.entity.task.TaskResult;
import com.renyigesai.maid_bakeries.entity.task.MaidTaskLinkedList;
import com.renyigesai.maid_bakeries.entity.task.MaidTaskNode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class MaidBakingTask extends MaidCheckRateTask {
    private static final int MAX_DELAY_TIME = 45;
    private final float speed;
    private final int closeEnoughDist;

    public MaidBakingTask(float speed, int closeEnoughDist) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
        this.speed = speed;
        this.closeEnoughDist = closeEnoughDist;
        this.setMaxCheckRate(MAX_DELAY_TIME);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        if (super.checkExtraStartConditions(worldIn, maid) && maid.canBrainMoving()) {
            MaidTaskLinkedList maidTaskLinkedList = BakingTasks.map.get(maid.getUUID());
            if (maidTaskLinkedList == null){
                return false;
            }
            maidTaskLinkedList.refreshAllPass(worldIn,maid);
            MaidTaskNode taskNode = maidTaskLinkedList.currentTaskNode();
            if (taskNode == null){
                return false;
            }
            AbstractCraftMaidTask task = taskNode.task;
            if (task == null){
                return false;
            }
            BlockPos pos = task.findBlock(worldIn, maid);
            if (pos != null && maid.isWithinRestriction(pos)) {
                if (pos.distToCenterSqr(maid.position()) < Math.pow(this.closeEnoughDist, 2)) {
                    maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(pos));
                    return true;
                }
                BehaviorUtils.setWalkAndLookTargetMemories(maid, pos, speed, 1);
                this.setNextCheckTickCount(5);
            } else {
                maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
            }
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long l) {
        MaidTaskLinkedList maidTaskLinkedList = BakingTasks.map.get(maid.getUUID());
        if (maidTaskLinkedList != null){
            if (maidTaskLinkedList.count == maidTaskLinkedList.repeatCount){
                return;
            }
            maidTaskLinkedList.refreshAllPass(level,maid);
            MaidTaskNode maidTaskNode = maidTaskLinkedList.currentTaskNode();
            if (maidTaskNode == null || maidTaskNode.task == null){
                return;
            }
            if (maidTaskNode.task.successFlag(level, maid)){
                maidTaskLinkedList.completeCurrentNode();
                maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
                setNextCheckTickCount(5);
                return;
            }
            maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).ifPresent(target -> {
                CombinedInvWrapper maidAvailableInv = maid.getAvailableInv(true);
                if (maidTaskNode.task.notBlockEntity()){
                    maidTaskNode.task.onCraft(level,maid,maidAvailableInv);
                    setNextCheckTickCount(5);
                    return;
                }
                BlockPos pos = target.currentBlockPosition();
                BlockEntity blockEntity = getBlockEntity(level,pos);
                TaskResult result = TaskResult.FAIL;
                if (maidTaskNode.task.isTake(blockEntity)){
                    result = maidTaskNode.task.take(blockEntity,maidAvailableInv,level);
                    setNextCheckTickCount(5);
                }
                if (result == TaskResult.FAIL){
                    maidTaskNode.task.put(blockEntity,maidAvailableInv,maid,level);
                    setNextCheckTickCount(100);
                }
                if (maidTaskNode.task.end){
                    if (!maidTaskNode.task.successFlag(level,maid)){
                        maidTaskLinkedList.repeatExecution();
                    }else {
                        maidTaskLinkedList.hand.next = null;
                    }
                }
            });
            maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
            maid.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
            maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
    }

    private BlockEntity getBlockEntity(ServerLevel level, BlockPos pos){
        return level.getBlockEntity(pos);
    }
}
