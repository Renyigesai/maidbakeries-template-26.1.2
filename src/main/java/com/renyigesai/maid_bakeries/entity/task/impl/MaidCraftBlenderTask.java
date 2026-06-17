package com.renyigesai.maid_bakeries.entity.task.impl;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.renyigesai.bakeries.common.blocks.blander.BlenderBlock;
import com.renyigesai.bakeries.common.blocks.blander.BlenderBlockEntity;
import com.renyigesai.bakeries.common.init.BakeriesBlocks;
import com.renyigesai.bakeries.common.init.BakeriesRecipes;
import com.renyigesai.bakeries.common.recipe.blender.BlenderRecipe;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.entity.task.TaskResult;
import com.renyigesai.maid_bakeries.init.MaidBakeriesTags;
import com.renyigesai.maid_bakeries.util.IORecipeAccessor;
import com.renyigesai.maid_bakeries.util.RecipeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
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
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

@SuppressWarnings("removal")
public class MaidCraftBlenderTask extends AbstractCraftMaidTask {

    @Override
    protected TagKey<PoiType> getPotType() {
        return MaidBakeriesTags.BLENDER;
    }

    public TaskResult put(BlockEntity blockEntity, CombinedResourceHandler<ItemResource> maidAvailableInv, EntityMaid maid, ServerLevel level){
        if (!(blockEntity instanceof BlenderBlockEntity blender)){
            return TaskResult.FAIL;
        }
        maid.swing(InteractionHand.MAIN_HAND);
        level.playSound(null,maid.blockPosition(), SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS);
        Recipe<?> recipe = RecipeUtils.getFirstRecipeByOutput(matchingStack, BakeriesRecipes.BLENDER_TYPE.get(), level);
        if (recipe != null){
            NonNullList<Ingredient> ingredients = IORecipeAccessor.getInput(recipe);
            List<ItemResource> stacks = RecipeUtils.getMatchList(ingredients,maidAvailableInv);
            if (stacks.size() == ingredients.size()){
                for (int slot = 0; slot < stacks.size(); slot++) {
                    ItemStack copy = stacks.get(slot).toStack();
                    copy.setCount(1);
                    blender.getInventory().setStackInSlot(slot,copy);
                    try (Transaction tx = Transaction.openRoot()) {
                        maidAvailableInv.extract(stacks.get(slot), 1, tx);
                        tx.commit();
                    }
                }
                return TaskResult.PASS;
            }
        }
        return TaskResult.FAIL;
    }

    @Override
    public TaskResult take(BlockEntity blockEntity, CombinedResourceHandler<ItemResource> maidAvailableInv, ServerLevel level) {
        if (!(blockEntity instanceof BlenderBlockEntity blender)){
            return TaskResult.FAIL;
        }
        ItemStack output = blender.getInventory().getStackInSlot(10);
        ItemStack result = ItemsUtil.insertItemStacked(maidAvailableInv, output, true,null);
        if (!result.isEmpty()){
            return TaskResult.FAIL;
        }
        ItemsUtil.insertItemStacked(maidAvailableInv, output.copy(), false,null);
        output.shrink(output.getCount());
        return TaskResult.PASS;
    }

    @Override
    public void onCraft(ServerLevel level, EntityMaid maid,CombinedResourceHandler<ItemResource> maidAvailableInv) {

    }

    @Override
    public boolean notBlockEntity() {
        return false;
    }

    @Override
    public Identifier getId() {
        return Identifier.fromNamespaceAndPath(MaidBakeries.MODID,"blender");
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
