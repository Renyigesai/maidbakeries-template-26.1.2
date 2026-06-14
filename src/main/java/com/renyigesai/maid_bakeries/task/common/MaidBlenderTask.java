package com.renyigesai.maid_bakeries.task.common;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.google.common.collect.ImmutableMap;
import com.renyigesai.bakeries.block.blender.BlenderBlock;
import com.renyigesai.bakeries.block.blender.BlenderBlockEntity;
import com.renyigesai.bakeries.init.BakeriesBlocks;
import com.renyigesai.maid_bakeries.init.MaidBakeriesTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nullable;
import java.util.Comparator;

public class MaidBlenderTask extends MaidCheckRateTask{
    private static final int MAX_DELAY_TIME = 45;
    private final float speed;
    private final int closeEnoughDist;

    public MaidBlenderTask(float speed, int closeEnoughDist) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
        this.speed = speed;
        this.closeEnoughDist = closeEnoughDist;
        this.setMaxCheckRate(MAX_DELAY_TIME);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        if (super.checkExtraStartConditions(worldIn, maid) && maid.canBrainMoving()) {
            BlockPos ovenPos = findBlender(worldIn, maid);
            if (ovenPos != null && maid.isWithinRestriction(ovenPos)) {
                if (ovenPos.distToCenterSqr(maid.position()) < Math.pow(this.closeEnoughDist, 2)) {
                    maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(ovenPos));
                    return true;
                }
                BehaviorUtils.setWalkAndLookTargetMemories(maid, ovenPos, speed, 1);
                this.setNextCheckTickCount(5);
            } else {
                maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
            }
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).ifPresent(target -> {
            BlockPos pos = target.currentBlockPosition();
            BlenderBlockEntity blenderEntity = getBlenderEntity(level, pos);
            if (blenderEntity == null){
                return;
            }
            CombinedInvWrapper maidAvailableInv = maid.getAvailableInv(true);
            this.putOrTakeOven(level,maid,maidAvailableInv,blenderEntity);
        });
        maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
        maid.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }


    @Nullable
    private BlockPos findBlender(ServerLevel world, EntityMaid maid) {
        BlockPos blockPos = maid.getBrainSearchPos();
        PoiManager poiManager = world.getPoiManager();
        int range = (int) maid.getRestrictRadius();
        return poiManager.getInRange(type -> type.is(MaidBakeriesTags.BLENDER), blockPos, range, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos).filter(pos -> canCollectBlender(world, pos))
                .min(Comparator.comparingDouble(pos -> pos.distSqr(maid.blockPosition()))).orElse(null);
    }

    private boolean canCollectBlender(ServerLevel world, BlockPos hivePos) {
        BlockState state = world.getBlockState(hivePos);
        return state.is(BakeriesBlocks.BLENDER.get()) && !state.getValue(BlenderBlock.POWERED);
    }

    private void putOrTakeOven(ServerLevel level, EntityMaid maid, CombinedInvWrapper maidAvailableInv,BlenderBlockEntity blender){
        int state = -1;/*初始状态*/
        if (!blender.getInventory().getStackInSlot(10).isEmpty()){/*如果输出槽有物品直接执行取*/
             state = take(blender.getInventory(),maidAvailableInv);/*如果成功取设置状态为0*/
        }
        if (state != 0){
            state = put(blender,maidAvailableInv);/*如果状态不为0,尝试执行放,如果成功放设置状态为1*/
        }

        if (state != -1){
            double x = maid.getX();
            double y = maid.getY();
            double z = maid.getZ();
            maid.swing(InteractionHand.MAIN_HAND);
            if (state == 0){
                this.setNextCheckTickCount(20);/*如果只是拿,立即重置为短时间的计时*/
                level.playSound(null, x, y, z, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }else {
                level.playSound(null, x, y, z, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }else {
            this.setNextCheckTickCount(200);/*如果全部不满足,设置一个长计时,让女仆休息一下*/
        }
    }

    private int put(BlenderBlockEntity blender,CombinedInvWrapper maidAvailableInv){
        int state = -1;
        ItemStackHandler filtrationinventory = blender.getFiltrationinventory();
        boolean b1 = false;
        for (int i = 0; i < filtrationinventory.getSlots() && !b1; i++) {
            ItemStack stackInSlot = filtrationinventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()){
                b1 = true;
            }
        }
        if (!b1){
            /*如果没有设置过滤物,直接返回放失败(-1)*/
            return state;
        }
        for (int i = 0; i < filtrationinventory.getSlots(); i++) {
            ItemStack input = filtrationinventory.getStackInSlot(i);
            if (!input.isEmpty()){
                boolean b2 = ItemsUtil.isStackIn(maidAvailableInv,stack -> stack.is(input.getItem()));
                boolean b3 = blender.getItem(i).isEmpty();
                if (b2 && b3){
                    ItemStack maidItem = ItemsUtil.getStack(maidAvailableInv, stack -> stack.is(input.getItem()));
                    ItemStack inputItem = maidItem.copy();
                    inputItem.setCount(1);
                    blender.getInventory().setStackInSlot(i,inputItem);
                    maidItem.shrink(1);
                    state = 1;
                }
            }
        }
        return state;
    }

    private int take(ItemStackHandler blenderInventory,CombinedInvWrapper maidAvailableInv){
        ItemStack output = blenderInventory.getStackInSlot(10);
        ItemStack result = ItemHandlerHelper.insertItemStacked(maidAvailableInv, output/*占位符物品*/, true/*假装放入*/);
        /*如果背包满了直接返回取失败(-1)*/
        if (!result.isEmpty()){
            return -1;
        }
        ItemHandlerHelper.insertItemStacked(maidAvailableInv, output.copy(), false);
        output.shrink(output.getCount());
        return 0;
    }

    private BlenderBlockEntity getBlenderEntity(ServerLevel level,BlockPos pos){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BlenderBlockEntity)){
            return null;
        }
        return (BlenderBlockEntity) blockEntity;
    }
}
