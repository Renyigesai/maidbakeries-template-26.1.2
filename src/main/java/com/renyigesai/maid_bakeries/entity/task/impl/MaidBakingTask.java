package com.renyigesai.maid_bakeries.entity.task.impl;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskIdle;
import com.github.tartaricacid.touhoulittlemaid.init.InitBrains;
import com.google.common.collect.ImmutableMap;
import com.renyigesai.maid_bakeries.MaidBakeries;
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
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class MaidBakingTask extends MaidCheckRateTask {
    private static final int MAX_DELAY_TIME = 45;
    private final float speed;
    private final int closeEnoughDist;

    public MaidBakingTask(float speed, int closeEnoughDist) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitBrains.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
        this.speed = speed;
        this.closeEnoughDist = closeEnoughDist;
        this.setMaxCheckRate(MAX_DELAY_TIME);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        try {
            /*基础条件检查：父类条件通过且女仆大脑允许移动 */
            if (super.checkExtraStartConditions(worldIn, maid) && maid.canBrainMoving()) {
                /*获取该女仆的任务链表，如果没有则不具备开始条件 */
                MaidTaskLinkedList maidTaskLinkedList = BakingTasks.map.get(maid.getUUID());
                if (maidTaskLinkedList == null) {
                    return false;
                }

                /*刷新链表中所有任务节点的通过状态 (根据 successFlag) */
                maidTaskLinkedList.refreshAllPass(worldIn, maid);

                /*获取当前第一个未完成的任务节点 */
                MaidTaskNode taskNode = maidTaskLinkedList.currentTaskNode();
                if (taskNode == null) {
                    return false;  /*所有任务已完成 */
                }

                /*获取节点中包含的具体任务 */
                AbstractCraftMaidTask task = taskNode.task;
                if (task == null) {
                    return false;  /*任务无效 */
                }

                /*寻找任务相关的工作站点 */
                BlockPos pos = task.findBlock(worldIn, maid);

                /*如果找到且在工作范围内 */
                if (pos != null && maid.isWithinHome(pos)) {
                    /*如果已经在工作站点附近足够距离，则设置目标位置并返回 true 表示可以开始 */
                    if (pos.distToCenterSqr(maid.position()) < Math.pow(this.closeEnoughDist, 2)) {
                        maid.getBrain().setMemory(InitBrains.TARGET_POS.get(), new BlockPosTracker(pos));
                        return true;
                    }

                    /*否则需要走过去，设定行走和看向目标，并缩短下次检查间隔 */
                    BehaviorUtils.setWalkAndLookTargetMemories(maid, pos, speed, 1);
                    this.setNextCheckTickCount(5);
                } else {
                    /*没有有效的工作站点，清除目标位置记忆 */
                    maid.getBrain().eraseMemory(InitBrains.TARGET_POS.get());
                }
            }
            return false;
        } catch (Exception e) {
            /*异常处理：重置女仆任务状态，防止 AI 卡死 */
            maid.setTask(new TaskIdle());
            BakingTasks.remove(maid.getUUID());
            MaidBakeries.LOGGER.error(e);
            return false;
        }
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long l) {
        try {
            /*从全局任务映射中获取当前女仆的任务链表*/
            MaidTaskLinkedList maidTaskLinkedList = BakingTasks.map.get(maid.getUUID());
            if (maidTaskLinkedList != null) {
                /*检查是否已达到预设的重复执行次数，如果已完成则直接返回*/
                if (maidTaskLinkedList.count == maidTaskLinkedList.repeatCount) {
                    return;
                }

                /*刷新链表中每个节点的通过状态（根据 successFlag 重新评估）*/
                maidTaskLinkedList.refreshAllPass(level, maid);

                /*获取第一个未通过的任务节点（即当前需要执行的任务）*/
                MaidTaskNode maidTaskNode = maidTaskLinkedList.currentTaskNode();
                if (maidTaskNode == null || maidTaskNode.task == null) {
                    return; /*所有任务已完成或节点无效*/
                }

                /*如果当前任务的目标已经满足（例如物品已足够），则直接标记为完成*/
                if (maidTaskNode.task.successFlag(level, maid)) {
                    maidTaskLinkedList.completeCurrentNode(); // 将当前节点标记为已通过
                    maid.getBrain().eraseMemory(InitBrains.TARGET_POS.get()); // 清除移动目标
                    setNextCheckTickCount(5); // 短时间内再次检查（执行下一个任务）
                    return;
                }

                /*如果有预设的目标位置（通常是工作站点，如烤炉），执行具体的交互逻辑*/
                maid.getBrain().getMemory(InitBrains.TARGET_POS.get()).ifPresent(target -> {
                    CombinedResourceHandler<ItemResource> maidAvailableInv = maid.getAvailableInv(true);

                    /*如果任务不需要方块实体（例如直接合成），则直接执行合成逻辑*/
                    if (maidTaskNode.task.notBlockEntity()) {
                        maidTaskNode.task.onCraft(level, maid, maidAvailableInv);
                        setNextCheckTickCount(5);
                        return;
                    }

                    /*获取目标位置的方块实体*/
                    BlockPos pos = target.currentBlockPosition();
                    BlockEntity blockEntity = getBlockEntity(level, pos);
                    TaskResult result = TaskResult.FAIL;

                    /*优先从方块实体中“取出”物品（如果 isTake 返回 true）*/
                    if (maidTaskNode.task.isTake(blockEntity)) {
                        result = maidTaskNode.task.take(blockEntity, maidAvailableInv, level);
                        setNextCheckTickCount(5);
                    }

                    /*如果取出操作失败或不需要取出，则执行“放入”物品（向机器供料）*/
                    if (result == TaskResult.FAIL) {
                        maidTaskNode.task.put(blockEntity, maidAvailableInv, maid, level);
                        setNextCheckTickCount(100); // 放入操作通常耗时更长，延长检查间隔
                    }

                    /*如果任务已标记为结束（end 为 true），进行最终判断*/
                    if (maidTaskNode.task.end) {
                        if (!maidTaskNode.task.successFlag(level, maid)) {
                            /*任务尚未达成目标，延长检查间隔，等待下一次尝试*/
                            setNextCheckTickCount(100);
                        } else {
                            /*任务成功完成，清空链表，重置女仆状态，移除全局任务记录*/
                            maidTaskLinkedList.hand.next = null;
                            /*切换回闲置状态*/
                            maid.setTask(new TaskIdle());
                            BakingTasks.remove(maid.getUUID());
                        }
                    }
                });
                /*交互完成后清除相关的脑记忆，避免女仆重复执行相同动作*/
                maid.getBrain().eraseMemory(InitBrains.TARGET_POS.get());
                maid.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
                maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            }
        } catch (Exception e) {
            /*发生任何异常时，安全地重置女仆任务状态，防止 AI 卡死*/
            maid.setTask(new TaskIdle());
            BakingTasks.remove(maid.getUUID());
            MaidBakeries.LOGGER.error(e);
        }
    }

    private BlockEntity getBlockEntity(ServerLevel level, BlockPos pos){
        return level.getBlockEntity(pos);
    }
}
