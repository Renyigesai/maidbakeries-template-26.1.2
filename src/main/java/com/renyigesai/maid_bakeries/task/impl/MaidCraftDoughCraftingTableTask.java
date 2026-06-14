package com.renyigesai.maid_bakeries.task.impl;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.renyigesai.bakeries.BakeriesMod;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.entity.task.TaskResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

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
    public TaskResult put(BlockEntity blockEntity, CombinedInvWrapper maidAvailableInv, EntityMaid maid,ServerLevel level) {
        return null;
    }

    @Override
    public TaskResult take(BlockEntity blockEntity, CombinedInvWrapper maidAvailableInv, ServerLevel level) {
        return null;
    }

    @Override
    public boolean notBlockEntity() {
        return true;
    }

    @Override
    public void onCraft(ServerLevel level, EntityMaid maid,CombinedInvWrapper maidAvailableInv){
        Optional<? extends Recipe<?>> recipeOptional = level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath(BakeriesMod.MODID, cacheRecipeId));
        maid.swing(InteractionHand.MAIN_HAND);
        level.playSound(null,maid.blockPosition(), SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS);
        if (recipeOptional.isPresent()){
            Recipe<?> recipe = recipeOptional.get();
            ItemStack item = recipe.getIngredients().get(0).getItems()[0];
            if (ItemsUtil.isStackIn(maidAvailableInv, stack -> stack.is(item.getItem()))){
                ItemStack result = ItemHandlerHelper.insertItemStacked(maidAvailableInv, matchingStack/*ռλ����Ʒ*/, true/*��װ����*/);
                if (!result.isEmpty()){
                    return;
                }
                ItemStack maidItem = ItemsUtil.getStack(maidAvailableInv, stack -> stack.is(item.getItem()));
                maidItem.shrink(1);
                ItemHandlerHelper.insertItemStacked(maidAvailableInv, matchingStack.copy(), false);
            }
        }
    }
}
