package com.renyigesai.maid_bakeries.entity.task.impl;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.entity.task.TaskResult;
import com.renyigesai.maid_bakeries.init.MaidBakeriesTags;
import com.renyigesai.maid_bakeries.util.IORecipeAccessor;
import com.renyigesai.maid_bakeries.util.RecipeUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class MaidCraftingTask extends AbstractCraftMaidTask {
    @Override
    protected TagKey<PoiType> getPotType() {
        return MaidBakeriesTags.CRAFTING_TABLE;
    }

    @Override
    public boolean isTake(BlockEntity blockEntity) {
        return false;
    }

    @Override
    public TaskResult put(BlockEntity blockEntity, CombinedResourceHandler<ItemResource> maidAvailableInv, EntityMaid maid, ServerLevel level) {
        return null;
    }

    @Override
    public TaskResult take(BlockEntity blockEntity, CombinedResourceHandler<ItemResource> maidAvailableInv, ServerLevel level) {
        return null;
    }

    @Override
    public void onCraft(ServerLevel level, EntityMaid maid, CombinedResourceHandler<ItemResource> maidAvailableInv) {
        Recipe<?> recipe = RecipeUtils.getFirstRecipeByOutput(matchingStack, RecipeType.CRAFTING, level);
        List<ItemStack> ingredients = List.of();
        if (recipe instanceof ShapelessRecipe){
            ingredients = RecipeUtils.getIngredientsFromRecipe(recipe, level);
        }
        if (!ingredients.isEmpty()){
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack input : ingredients) {
                if (!input.isEmpty()) {
                    if (ItemsUtil.isStackIn(maidAvailableInv, stack -> stack.is(input.getItem()))) {
                        stacks.add(ItemsUtil.getStack(maid, stack -> stack.is(input.getItem())));
                    } else {
                        return;
                    }
                }
            }
            ItemStack output = IORecipeAccessor.getOutput(recipe);
            for (ItemStack stack : stacks) {
                try (Transaction tx = Transaction.openRoot()) {
                    maidAvailableInv.extract(ItemResource.of(stack), 1, tx);
                    tx.commit();
                }
            }
            ItemsUtil.insertItemStacked(maidAvailableInv, output.copy(), false,null);
        }
    }

    @Override
    public boolean notBlockEntity() {
        return true;
    }

    @Override
    public Identifier getId() {
        return Identifier.fromNamespaceAndPath(MaidBakeries.MODID,"crafting_shapeless");
    }


}
