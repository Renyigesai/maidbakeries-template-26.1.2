package com.renyigesai.maid_bakeries.entity.task.impl;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.renyigesai.bakeries.BakeriesMod;
import com.renyigesai.bakeries.common.recipe.DoughCraftingTableRecipe;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.entity.task.TaskResult;
import com.renyigesai.maid_bakeries.init.MaidBakeriesTags;
import com.renyigesai.maid_bakeries.util.IORecipeAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Optional;

public class MaidCraftDoughCraftingTableTask extends AbstractCraftMaidTask {

    @Override
    protected TagKey<PoiType> getPotType() {
        return PoiTypeTags.ACQUIRABLE_JOB_SITE;
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
    public boolean notBlockEntity() {
        return true;
    }

    @Override
    public void onCraft(ServerLevel level, EntityMaid maid,CombinedResourceHandler<ItemResource> maidAvailableInv){
        if (level.recipeAccess() instanceof RecipeManager recipeManager){
            Optional<RecipeHolder<?>> recipeOptional = recipeManager.byKey(ResourceKey.create(Registries.RECIPE, Identifier.parse(cacheRecipeId)));
            maid.swing(InteractionHand.MAIN_HAND);
            level.playSound(null,maid.blockPosition(), SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS);
            if (recipeOptional.isPresent()){
                Recipe<?> recipe = recipeOptional.get().value();
                Holder<Item> itemHolder = ((DoughCraftingTableRecipe) recipe).input().getValues().get(0);
                ItemStack item = new ItemStack(itemHolder);
                if (ItemsUtil.isStackIn(maidAvailableInv, stack -> stack.is(item.getItem()))){
                    ItemStack output = IORecipeAccessor.getOutput(recipe);ItemStack result = ItemsUtil.insertItemStacked(maidAvailableInv, output, true,null);
                    if (!result.isEmpty()){
                        return;
                    }
                    ItemStack maidItem = ItemsUtil.getStack(maidAvailableInv, stack -> stack.is(item.getItem()));
                    try (Transaction tx = Transaction.openRoot()){
                        maidAvailableInv.extract(ItemResource.of(maidItem), 1, tx);
                        tx.commit();
                    }
                    ItemsUtil.insertItemStacked(maidAvailableInv, output.copy(), false,null);
                }
            }
        }
    }
}
