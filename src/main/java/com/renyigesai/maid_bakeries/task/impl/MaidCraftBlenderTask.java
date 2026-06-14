package com.renyigesai.maid_bakeries.task.impl;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.renyigesai.bakeries.api.block.BakeriesWorkBlock;
import com.renyigesai.bakeries.block.blender.BlenderBlock;
import com.renyigesai.bakeries.block.blender.BlenderBlockEntity;
import com.renyigesai.bakeries.init.BakeriesBlocks;
import com.renyigesai.bakeries.recipe.BlenderRecipe;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.entity.task.TaskResult;
import com.renyigesai.maid_bakeries.init.MaidBakeriesTags;
import com.renyigesai.maid_bakeries.util.RecipeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.util.ArrayList;
import java.util.List;

public class MaidCraftBlenderTask extends AbstractCraftMaidTask {

    @Override
    protected TagKey<PoiType> getPotType() {
        return MaidBakeriesTags.BLENDER;
    }

    public TaskResult put(BlockEntity blockEntity, CombinedInvWrapper maidAvailableInv, EntityMaid maid,ServerLevel level){
        if (!(blockEntity instanceof BlenderBlockEntity blender)){
            return TaskResult.FAIL;
        }
        maid.swing(InteractionHand.MAIN_HAND);
        level.playSound(null,maid.blockPosition(),blender.getOpenSound(), SoundSource.BLOCKS);
        Recipe<?> recipe = RecipeUtils.getFirstRecipeByOutput(matchingStack, BlenderRecipe.Type.INSTANCE, level);
        if (recipe != null){
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            List<ItemStack> stacks = RecipeUtils.getMatchList(ingredients,maidAvailableInv);
            if (stacks.size() == ingredients.size()){
                for (int slot = 0; slot < stacks.size(); slot++) {
                    ItemStack copy = stacks.get(slot).copy();
                    copy.setCount(1);
                    blender.getInventory().setStackInSlot(slot,copy);
                    stacks.get(slot).shrink(1);
                }
                return TaskResult.PASS;
            }
        }
        return TaskResult.FAIL;
    }

    @Override
    public TaskResult take(BlockEntity blockEntity, CombinedInvWrapper maidAvailableInv, ServerLevel level) {
        if (!(blockEntity instanceof BlenderBlockEntity blender)){
            return TaskResult.FAIL;
        }
        ItemStack output = blender.getInventory().getStackInSlot(10);
        ItemStack result = ItemHandlerHelper.insertItemStacked(maidAvailableInv, output/*ռλ����Ʒ*/, true/*��װ����*/);
        if (!result.isEmpty()){
            return TaskResult.FAIL;
        }
        ItemHandlerHelper.insertItemStacked(maidAvailableInv, output.copy(), false);
        output.shrink(output.getCount());
        return TaskResult.PASS;
    }

    @Override
    public void onCraft(ServerLevel level, EntityMaid maid,CombinedInvWrapper maidAvailableInv) {

    }

    @Override
    public boolean notBlockEntity() {
        return false;
    }

    @Override
    public boolean isTake(BlockEntity blockEntity) {
        if (!(blockEntity instanceof BlenderBlockEntity blender)){
            return false;
        }
        return blender.getInventory().getStackInSlot(10).is(matchingStack.getItem());
    }

    @Override
    public boolean canCollect(ServerLevel world, BlockPos hivePos) {
        BlockState state = world.getBlockState(hivePos);
        return state.is(BakeriesBlocks.BLENDER.get()) && !state.getValue(BlenderBlock.POWERED);
    }
}
