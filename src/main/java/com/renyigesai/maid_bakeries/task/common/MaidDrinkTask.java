package com.renyigesai.maid_bakeries.task.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.google.common.collect.ImmutableMap;
import com.renyigesai.bakeries.block.glass_drink_cup.GlassDrinkCupBlockEntity;
import com.renyigesai.bakeries.init.BakeriesSounds;
import com.renyigesai.bakeries.util.ItemUtils;
import com.renyigesai.maid_bakeries.item.DrinkStickyNoteItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.util.List;
import java.util.Optional;

public class MaidDrinkTask extends Behavior<EntityMaid> {
    private static final int MAX_PROBABILITY = 15;
    private int maxCheckRate = 20;
    private int nextCheckTickCount;
    private final double closeEnoughDist;

    public MaidDrinkTask(double closeEnoughDist) {
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT));
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid owner) {
        if (this.nextCheckTickCount > 0) {
            --this.nextCheckTickCount;
            return false;
        } else {
            this.nextCheckTickCount = this.maxCheckRate + owner.getRandom().nextInt(this.maxCheckRate);
            Brain<EntityMaid> brain = owner.getBrain();
            return brain.getMemory(InitEntities.TARGET_POS.get()).map(targetPos -> {
                Vec3 targetV3d = targetPos.currentPosition();
                if (owner.distanceToSqr(targetV3d) > Math.pow(closeEnoughDist, 2)) {
                    Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
                    if (!walkTarget.isPresent() || !walkTarget.get().getTarget().currentPosition().equals(targetV3d)) {
                        brain.eraseMemory(InitEntities.TARGET_POS.get());
                    }
                    return false;
                }
                return true;
            }).orElse(false);
        }
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTimeIn) {
        maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).ifPresent(posWrapper -> {
            BlockPos pos = posWrapper.currentBlockPosition();
            BlockState blockState = world.getBlockState(pos);
            CombinedInvWrapper maidAvailableInv = maid.getAvailableInv(true);
            GlassDrinkCupBlockEntity cupEntity = this.getCupEntity(world, pos);
            if (cupEntity != null){
                putOrTakeCup(world,maid,maidAvailableInv,blockState,pos,cupEntity);
            }
            maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
            maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        });
    }

    private void putOrTakeCup(ServerLevel level, EntityMaid maid, CombinedInvWrapper maidAvailableInv, BlockState hiveBlockState, BlockPos cupPos, GlassDrinkCupBlockEntity cup){
        ItemStack mainHandItem = maid.getMainHandItem();
        List<ItemStack> inventoryList = DrinkStickyNoteItem.getInventoryList(mainHandItem);
        if (inventoryList.isEmpty()){
            return;
        }
        int state = -1;
        state = take(cup,level,inventoryList,cupPos,state);
        if (state != 0){
            for (int i = 0; i < 4; i++) {
                ItemStack input = inventoryList.get(i);
                if (ItemsUtil.isStackIn(maid.getAvailableInv(false), stack -> stack.is(input.getItem()))){
                    ItemStack maidItem = ItemsUtil.getStack(maidAvailableInv, stack -> stack.is(input.getItem()));
                    cup.getInventory().setStackInSlot(i,maidItem.copy());
                    maidItem.shrink(1);
                    state = 1;
                }
            }
            cup.setChanged();
            cup.forcedRefresh();
            cup.setChanged();
            level.sendBlockUpdated(cupPos, hiveBlockState, hiveBlockState, 3);
        }
        if (state != -1){
            double x = maid.getX();
            double y = maid.getY();
            double z = maid.getZ();
            maid.swing(InteractionHand.MAIN_HAND);
            if (state == 0){
                this.setMaxCheckRate(20);//如果只是拿，立即重置为短时间的计时
                level.playSound(null, x, y, z, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }else {
                level.playSound(null, x, y, z, BakeriesSounds.PUT_ON_ICE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }else {
            this.setMaxCheckRate(200);//如果全部不满足,设置一个长计时，让女仆休息一下
        }
    }

    private int take(GlassDrinkCupBlockEntity cup,ServerLevel level,List<ItemStack> list,BlockPos pos,int state){
        if (cup.getInventory().getStackInSlot(4).is(list.get(4).getItem())){
            cup.removeItems();

            Item var6 = list.get(4).getItem();
            if (var6 instanceof BlockItem blockItem) {
                if (!level.getBlockState(pos.below()).is(Blocks.HOPPER)) {
                    level.setBlock(pos, blockItem.getBlock().defaultBlockState(), 3);
                    return 0;
                }
            }

            ItemUtils.spawnItemEntity(level, list.get(4), (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, new Vec3(0.0, 0.0, 0.0));
            level.removeBlock(pos, false);
            return 0;
        }
        return state;
    }

    private GlassDrinkCupBlockEntity getCupEntity(ServerLevel level,BlockPos pos){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof GlassDrinkCupBlockEntity)){
            return null;
        }
        return (GlassDrinkCupBlockEntity) blockEntity;
    }

    protected void setMaxCheckRate(int maxCheckRate) {
        this.maxCheckRate = maxCheckRate;
    }
}
