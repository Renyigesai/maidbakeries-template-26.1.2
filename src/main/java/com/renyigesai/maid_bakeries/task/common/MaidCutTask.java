package com.renyigesai.maid_bakeries.task.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import com.renyigesai.bakeries.api.block.AKnifeCutBlock;
import com.renyigesai.bakeries.api.block.IKnifeCutBlock;
import com.renyigesai.bakeries.init.BakeriesItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class MaidCutTask extends Behavior<EntityMaid> {
    private final double closeEnoughDist;

    public MaidCutTask(double closeEnoughDist) {
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT));
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid owner) {
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

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTimeIn) {
        maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).ifPresent(posWrapper -> {
                BlockPos pos = posWrapper.currentBlockPosition();
                BlockState blockState = world.getBlockState(pos);
                cut(world,blockState,pos,maid,maid.getMainHandItem(),EquipmentSlot.MAINHAND);
                maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
                maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        });
    }

    private void cut(Level level, BlockState state, BlockPos pos , EntityMaid maid, ItemStack hand, EquipmentSlot slot){
        /*手上没有刀不能切！*/
        @SuppressWarnings("all")
        boolean flag1 = maid.getMainHandItem().is(BakeriesItems.BREAD_KNIFE.get()) || maid.getMainHandItem().is(ItemTags.create(new ResourceLocation("forge:tools/knives")));
        if (!flag1){
            return;
        }
        /*获取方块是否拥有PILE状态,如果拥有需在1的时候切*/
        boolean flag2;
        int i = state.getBlock().getStateDefinition().getProperty("pile") instanceof IntegerProperty i2 ? state.getValue(i2) : -1;
        if (i == -1){
            flag2 = true;
        }else {
            flag2 = i != 2;
        }
        if (flag2){
            if (state.getBlock() instanceof AKnifeCutBlock cutA) {
                cutA.cut(level, state, pos, maid, hand, slot);
            }else if (state.getBlock() instanceof IKnifeCutBlock cutI){
                cutI.cut(level, state, pos, maid, hand, slot);
            }
            level.playSound(null,maid.getX(),maid.getY(),maid.getZ(), SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 0.8F, 0.8F);
            maid.swing(InteractionHand.MAIN_HAND);
        }
    }
}
