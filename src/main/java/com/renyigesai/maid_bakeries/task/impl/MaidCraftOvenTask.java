package com.renyigesai.maid_bakeries.task.impl;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.renyigesai.bakeries.BakeriesMod;
import com.renyigesai.bakeries.block.blender.BlenderBlock;
import com.renyigesai.bakeries.block.oven.OvenBlock;
import com.renyigesai.bakeries.block.oven.OvenBlockEntity;
import com.renyigesai.bakeries.init.BakeriesBlocks;
import com.renyigesai.bakeries.recipe.oven.OvenRecipe;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.entity.task.TaskResult;
import com.renyigesai.maid_bakeries.init.MaidBakeriesTags;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.util.Optional;

public class MaidCraftOvenTask extends AbstractCraftMaidTask {

    @Override
    protected TagKey<PoiType> getPotType() {
        return MaidBakeriesTags.OVEN;
    }

    @Override
    public boolean isTake(BlockEntity blockEntity) {
        if (blockEntity instanceof OvenBlockEntity oven){
            for (int i = 0; i < oven.getItemHandler().getSlots(); i++) {
                if (oven.getItemHandler().getStackInSlot(i).is(matchingStack.getItem())){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public TaskResult put(BlockEntity blockEntity, CombinedInvWrapper maidAvailableInv, EntityMaid maid,ServerLevel level) {
        if (!(blockEntity instanceof OvenBlockEntity oven)){
            return TaskResult.FAIL;
        }
        maid.swing(InteractionHand.MAIN_HAND);
        level.playSound(null,maid.blockPosition(),oven.getOpenSound(), SoundSource.BLOCKS);
        Optional<? extends Recipe<?>> recipeOptional = level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath(BakeriesMod.MODID, cacheRecipeId));
        TaskResult state = TaskResult.FAIL;
        if (recipeOptional.isPresent()){
            Recipe<?> recipe = recipeOptional.get();
            int perfectTemperature = ((OvenRecipe) recipe).getPerfectTemperature() != 0 ? ((OvenRecipe) recipe).getPerfectTemperature() : ((OvenRecipe) recipe).getMaxTemperature();
            oven.setTemperature(oven,perfectTemperature);
            for (int i = 0; i < oven.getItemHandler().getSlots(); i++) {
                ItemStack stackInSlot = oven.getItemHandler().getStackInSlot(i);
                Item item = recipe.getIngredients().get(0).getItems()[0].getItem();
                boolean b1 = stackInSlot.isEmpty();
                boolean b2 = ItemsUtil.isStackIn(maidAvailableInv, stack -> stack.is(item));
                if (b1 && b2){
                    ItemStack maidItem = ItemsUtil.getStack(maidAvailableInv, stack -> stack.is(item));
                    oven.setItem(i,maidItem.copy());
                    maidItem.shrink(1);
                    state = TaskResult.PASS;
                }
            }
            setNextCheckTickCount(((OvenRecipe) recipe).getTime());
        }
        return state;
    }

    @Override
    public TaskResult take(BlockEntity blockEntity, CombinedInvWrapper maidAvailableInv, ServerLevel level) {
        if (!(blockEntity instanceof OvenBlockEntity oven)){
            return TaskResult.FAIL;
        }
        TaskResult state = TaskResult.FAIL;
        for (int i = 0; i < oven.getItemHandler().getSlots(); i++) {
            ItemStack item = oven.getItem(i);
            if (!item.isEmpty() && item.is(matchingStack.getItem())){
                ItemStack result = ItemHandlerHelper.insertItemStacked(maidAvailableInv, item/*ռλ����Ʒ*/, true/*��װ����*/);
                if (!result.isEmpty()){
                    break;
                }
                ItemHandlerHelper.insertItemStacked(maidAvailableInv, item.copy(), false);
                oven.getItemHandler().setStackInSlot(i,ItemStack.EMPTY);
                OvenBlockEntity.updateBlock(oven);
                state = TaskResult.PASS;
            }
        }
        return state;
    }

    @Override
    public void onCraft(ServerLevel level, EntityMaid maid,CombinedInvWrapper maidAvailableInv) {

    }

    @Override
    public boolean notBlockEntity() {
        return false;
    }

    @Override
    public boolean canCollect(ServerLevel world, BlockPos hivePos) {
        BlockState state = world.getBlockState(hivePos);
        return state.is(BakeriesBlocks.OVEN.get()) && !state.getValue(OvenBlock.LIT);
    }

}
