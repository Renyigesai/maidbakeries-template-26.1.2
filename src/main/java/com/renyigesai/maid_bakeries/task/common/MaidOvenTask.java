package com.renyigesai.maid_bakeries.task.common;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.google.common.collect.ImmutableMap;
import com.renyigesai.bakeries.block.oven.OvenBlock;
import com.renyigesai.bakeries.block.oven.OvenBlockEntity;
import com.renyigesai.bakeries.init.BakeriesBlocks;
import com.renyigesai.bakeries.init.BakeriesSounds;
import com.renyigesai.maid_bakeries.init.MaidBakeriesTags;
import com.renyigesai.maid_bakeries.item.OvenStickyNoteItem;
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
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class MaidOvenTask extends MaidCheckRateTask{
    private static final int MAX_DELAY_TIME = 45;
    private final float speed;
    private final int closeEnoughDist;

    public MaidOvenTask(float speed, int closeEnoughDist) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
        this.speed = speed;
        this.closeEnoughDist = closeEnoughDist;
        this.setMaxCheckRate(MAX_DELAY_TIME);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        if (super.checkExtraStartConditions(worldIn, maid) && maid.canBrainMoving()) {
            BlockPos ovenPos = findOven(worldIn, maid);
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
            BlockPos hivePos = target.currentBlockPosition();
            BlockState hiveBlockState = level.getBlockState(hivePos);
            CombinedInvWrapper maidAvailableInv = maid.getAvailableInv(true);
            ItemStack note = maid.getMainHandItem();
            int temperature = note.getOrCreateTag().getInt("Temperature");
            OvenBlockEntity ovenEntity = this.getOvenEntity(level, hivePos);
            if (ovenEntity == null){
                return;
            }
            if (ovenEntity.temperature != temperature){
                ovenEntity.setTemperature(ovenEntity,temperature);
            }
            this.putOrTakeOven(level, maid, maidAvailableInv, hiveBlockState, hivePos,ovenEntity);
        });
        maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
        maid.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }


    @Nullable
    private BlockPos findOven(ServerLevel world, EntityMaid maid) {
        BlockPos blockPos = maid.getBrainSearchPos();
        PoiManager poiManager = world.getPoiManager();
        int range = (int) maid.getRestrictRadius();
        return poiManager.getInRange(type -> type.is(MaidBakeriesTags.OVEN), blockPos, range, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos).filter(pos -> canCollectOven(world, pos))
                .min(Comparator.comparingDouble(pos -> pos.distSqr(maid.blockPosition()))).orElse(null);
    }

    private boolean canCollectOven(ServerLevel world, BlockPos hivePos) {
        BlockState state = world.getBlockState(hivePos);
        return state.is(BakeriesBlocks.OVEN.get()) && !state.getValue(OvenBlock.LIT);
    }

    private void putOrTakeOven(ServerLevel level, EntityMaid maid, CombinedInvWrapper maidAvailableInv, BlockState hiveBlockState, BlockPos ovenPos,OvenBlockEntity oven){
        ItemStack mainHandItem = maid.getMainHandItem();
        List<ItemStack> inventoryList = OvenStickyNoteItem.getInventoryList(mainHandItem);
        if (inventoryList.isEmpty()){
            return;
        }
        int state = -1;
        if (isOvenFull(oven)){
            state = take(oven,inventoryList,maidAvailableInv);
        }
        if (state != 0){
            state = take(oven,inventoryList,maidAvailableInv);
            if (state != 0 && ItemsUtil.isStackIn(maid.getAvailableInv(false), stack -> stack.is(inventoryList.get(0).getItem()))){
                ItemStack input = ItemsUtil.getStack(maidAvailableInv, stack -> stack.is(inventoryList.get(0).getItem()));
                for (int i = 0; i < 4; i++) {
                    if (oven.getItem(i).isEmpty()){
                        oven.setItem(i,input.copy());
                        input.shrink(1);
                    }
                }
                state = 1;
            }

        }
        if (state != -1){
            double x = maid.getX();
            double y = maid.getY();
            double z = maid.getZ();
            maid.swing(InteractionHand.MAIN_HAND);
            level.playSound(null, x, y, z, BakeriesSounds.OVEN_OPEN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            if (state == 0){
                this.setNextCheckTickCount(20);//如果只是拿，立即重置为短时间的计时
                level.playSound(null, x, y, z, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }else {
                level.playSound(null, x, y, z, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }else {
            this.setNextCheckTickCount(200);//如果全部不满足,设置一个长计时，让女仆休息一下
        }
    }

    private int take(OvenBlockEntity oven,List<ItemStack> output,CombinedInvWrapper maidAvailableInv){
        int state = -1;
        for (int i = 0; i < 4; i++) {
            ItemStack item = oven.getItem(i);
            if (!item.isEmpty() && item.is(output.get(1).getItem())){
                ItemStack result = ItemHandlerHelper.insertItemStacked(maidAvailableInv, item/*占位符物品*/, true/*假装放入*/);
                if (!result.isEmpty()){
                    break;
                }
                ItemHandlerHelper.insertItemStacked(maidAvailableInv, item.copy(), false);
                item.shrink(1);
                state = 0;
            }
        }
        return state;
    }

    private OvenBlockEntity getOvenEntity(ServerLevel level,BlockPos pos){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof OvenBlockEntity)){
            return null;
        }
        return (OvenBlockEntity) blockEntity;
    }

    private boolean isOvenFull(OvenBlockEntity oven){
        return !oven.getItem(0).isEmpty() && !oven.getItem(1).isEmpty() && !oven.getItem(2).isEmpty() && !oven.getItem(3).isEmpty();
    }

}
