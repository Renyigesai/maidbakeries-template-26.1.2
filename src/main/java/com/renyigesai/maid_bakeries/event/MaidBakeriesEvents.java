package com.renyigesai.maid_bakeries.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskIdle;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.data.BakingTasks;
import com.renyigesai.maid_bakeries.data.RecipeCatalog;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.entity.task.MaidTaskLinkedList;
import com.renyigesai.maid_bakeries.entity.task.MaidTaskNode;
import com.renyigesai.maid_bakeries.item.CraftOrderItem;
import com.renyigesai.maid_bakeries.entity.task.TaskBaking;
import com.renyigesai.maid_bakeries.util.RecipeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class MaidBakeriesEvents {

    @SubscribeEvent
    public static void onUseMaid(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof EntityMaid maid) {
            if (maid.getTask().getUid() == TaskBaking.UID) {
                ItemStack itemInHand = event.getEntity().getItemInHand(InteractionHand.MAIN_HAND);
                if (itemInHand.getItem() instanceof CraftOrderItem && !canOwner(maid, event.getEntity())) {
                    event.setCanceled(true);
                    return;
                }
                if ((itemInHand.getItem() instanceof CraftOrderItem) && BakingTasks.map.get(maid.getUUID()) != null){
                    event.getEntity().sendOverlayMessage(Component.translatable("tooltip.maid_bakeries.no_maid_set_task_2").withStyle(ChatFormatting.DARK_RED));
                    event.setCanceled(true);
                    return;
                }
                setTask(event,itemInHand,maid);
            }
        }
    }

    private static boolean canOwner(EntityMaid maid,Player player){
        if (maid.getTask().getUid() == TaskBaking.UID){
            return maid.getOwner() != null && maid.getOwner() == player;
        }
        player.sendOverlayMessage(Component.translatable("tooltip.maid_bakeries.no_maid_set_task_1").withStyle(ChatFormatting.DARK_RED));
        return false;
    }

    private static void setTask(PlayerInteractEvent.EntityInteract event,ItemStack orderItem,EntityMaid maid){
        ItemStack matchingStack = CraftOrderItem.getEnd(orderItem);
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (matchingStack.isEmpty()) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        try {
            List<AbstractCraftMaidTask> tasks = new ArrayList<>();

            List<String> types = CraftOrderItem.getTypes(orderItem);
            List<ItemStack> inventoryList = CraftOrderItem.getInventoryList(orderItem);
            int size = types.size();
            for (int i = 0; i < size; i++) {
                String type = types.get(i);
                ItemStack resultStack = inventoryList.get(i);
                RecipeType<?> recipeType = RecipeCatalog.RECIPE_TYPES.get(type);
                if (recipeType != null && RecipeUtils.isResultItem(event.getLevel(),recipeType,resultStack)){
                    AbstractCraftMaidTask maidCraftTask = RecipeCatalog.TASKS.get(type).get();
                    if (maidCraftTask != null){
                        maidCraftTask.setMatchingStack(resultStack);
                        RecipeHolder<?> firstRecipeHolderByOutput = RecipeUtils.getFirstRecipeHolderByOutput(resultStack, recipeType, event.getLevel());
                        if (firstRecipeHolderByOutput != null){
                            maidCraftTask.setCacheRecipeId(String.valueOf(firstRecipeHolderByOutput.id().identifier()));
                            maidCraftTask.targetCount = resultStack.count();
                            tasks.add(maidCraftTask);
                        }
                    }
                }
            }
            if (tasks.size() > 5) {
                tasks.subList(5, tasks.size()).clear();
            }
            tasks.get(tasks.size()-1).end = true;
            MaidTaskLinkedList maidTaskLinkedList = new MaidTaskLinkedList();
            maidTaskLinkedList.repeatCount = 1;
            for (AbstractCraftMaidTask task : tasks) {
                maidTaskLinkedList.add(new MaidTaskNode(task));
            }
            if (maidTaskLinkedList.hand != null){
                BakingTasks.add(maid.getUUID(), maidTaskLinkedList);
            }
            BlockPos pos = maid.blockPosition();
            event.getLevel().playSound(null,pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS);
        } catch (Exception e) {
            maid.setTask(new TaskIdle());
            BakingTasks.remove(maid.getUUID());
            MaidBakeries.LOGGER.error(e);
        }
    }

    @SubscribeEvent
    public static void onMaidRemoved(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof EntityMaid maid) {
            BakingTasks.remove(maid.getUUID());
        }
    }
}
