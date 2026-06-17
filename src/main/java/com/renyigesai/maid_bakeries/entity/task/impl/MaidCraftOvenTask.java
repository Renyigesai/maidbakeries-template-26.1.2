package com.renyigesai.maid_bakeries.entity.task.impl;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.renyigesai.bakeries.BakeriesMod;
import com.renyigesai.bakeries.common.blocks.oven.OvenBlock;
import com.renyigesai.bakeries.common.blocks.oven.OvenBlockEntity;
import com.renyigesai.bakeries.common.init.BakeriesBlocks;
import com.renyigesai.bakeries.common.recipe.OvenRecipe;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.entity.task.TaskResult;
import com.renyigesai.maid_bakeries.init.MaidBakeriesTags;
import com.renyigesai.maid_bakeries.util.IORecipeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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
    public TaskResult put(BlockEntity blockEntity, CombinedResourceHandler<ItemResource> maidAvailableInv, EntityMaid maid, ServerLevel level) {
        if (!(blockEntity instanceof OvenBlockEntity oven)){
            return TaskResult.FAIL;
        }
        maid.swing(InteractionHand.MAIN_HAND);
        level.playSound(null,maid.blockPosition(), SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS);
        if (level.recipeAccess() instanceof RecipeManager recipeManager){
            Optional<RecipeHolder<?>> recipeOptional = recipeManager.byKey(ResourceKey.create(Registries.RECIPE, Identifier.parse(cacheRecipeId)));
            TaskResult state = TaskResult.FAIL;
            if (recipeOptional.isPresent()){
                Recipe<?> recipe = recipeOptional.get().value();
                int perfectTemperature = ((OvenRecipe) recipe).getPerfectTemperature() != 0 ? ((OvenRecipe) recipe).getPerfectTemperature() : ((OvenRecipe) recipe).getMaxTemperature();
                oven.setTemperature(perfectTemperature);
                for (int i = 0; i < oven.getItemHandler().getSlots(); i++) {
                    ItemStack stackInSlot = oven.getItemHandler().getStackInSlot(i);
                    Item item = IORecipeAccessor.getInput(recipe).get(0).getValues().get(0).value();
                    boolean b1 = stackInSlot.isEmpty();
                    boolean b2 = ItemsUtil.isStackIn(maidAvailableInv, stack -> stack.is(item));
                    if (b1 && b2){
                        ItemStack maidItem = ItemsUtil.getStack(maidAvailableInv, stack -> stack.is(item));
                        ItemStack maidItemCopy = maidItem.copy();
                        maidItemCopy.setCount(1);
                        oven.setItem(i,maidItemCopy);
                        try (Transaction tx = Transaction.openRoot()){
                            maidAvailableInv.extract(ItemResource.of(maidItem),1,tx);
                            tx.commit();
                        }
                        state = TaskResult.PASS;
                    }
                }
                setNextCheckTickCount(((OvenRecipe) recipe).getCookingTime());
                return state;
            }
        }
        return TaskResult.FAIL;
    }

    @Override
    public TaskResult take(BlockEntity blockEntity, CombinedResourceHandler<ItemResource> maidAvailableInv, ServerLevel level) {
        if (!(blockEntity instanceof OvenBlockEntity oven)){
            return TaskResult.FAIL;
        }
        TaskResult state = TaskResult.FAIL;
        for (int i = 0; i < oven.getItemHandler().getSlots(); i++) {
            ItemStack item = oven.getItem(i);
            if (!item.isEmpty() && item.is(matchingStack.getItem())){
                ItemStack result = ItemsUtil.insertItemStacked(maidAvailableInv, item/*ռλ����Ʒ*/, true/*��װ����*/,null);
                if (!result.isEmpty()){
                    break;
                }
                ItemsUtil.insertItemStacked(maidAvailableInv, item.copy(), false,null);
                oven.getItemHandler().setStackInSlot(i,ItemStack.EMPTY);
                OvenBlockEntity.updateBlock(oven);
                state = TaskResult.PASS;
            }
        }
        return state;
    }

    @Override
    public void onCraft(ServerLevel level, EntityMaid maid,CombinedResourceHandler<ItemResource> maidAvailableInv) {

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

    @Override
    public Identifier getId() {
        return Identifier.fromNamespaceAndPath(MaidBakeries.MODID,"oven");
    }

}
