package com.renyigesai.maid_bakeries.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.renyigesai.maid_bakeries.data.BakingTasks;
import com.renyigesai.maid_bakeries.data.RecipeCatalog;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.item.CraftOrderItem;
import com.renyigesai.maid_bakeries.task.TaskBaking;
import com.renyigesai.maid_bakeries.entity.task.MaidTaskLinkedList;
import com.renyigesai.maid_bakeries.entity.task.MaidTaskNode;
import com.renyigesai.maid_bakeries.util.RecipeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber
public class MaidBakeriesEvents {

    @SubscribeEvent
    public static void onUseMaid(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof EntityMaid maid) {
            if (maid.getTask().getUid() == TaskBaking.UID) {
                ItemStack itemInHand = event.getEntity().getItemInHand(InteractionHand.MAIN_HAND);
                if (!(itemInHand.getItem() instanceof CraftOrderItem) || !canOwner(maid, event.getEntity())) {
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
        player.displayClientMessage(Component.translatable("tooltip.maid_bakeries.no_maid_set_task").withStyle(ChatFormatting.DARK_RED),true);
        return false;
    }

    private static void setTask(PlayerInteractEvent.EntityInteract event,ItemStack itemInHand,EntityMaid maid){
        ItemStack matchingStack = CraftOrderItem.getEnd(itemInHand);
        if (matchingStack.isEmpty()) {
            return;
        }
        if (event.getLevel().isClientSide) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        List<AbstractCraftMaidTask> tasks = new ArrayList<>();
        int count = CraftOrderItem.getRepeat(itemInHand);
        for (RecipeType<?> recipeType : RecipeCatalog.RECIPE_TYPES) {
            if (RecipeUtils.isResultItem(event.getLevel(), recipeType, matchingStack)) {
                AbstractCraftMaidTask maidCraftTask = RecipeCatalog.TASKS.get(recipeType);
                Recipe<?> firstRecipeByOutput = RecipeUtils.getFirstRecipeByOutput(matchingStack, recipeType, event.getLevel());
                String path = firstRecipeByOutput.getId().getPath();
                maidCraftTask.setMatchingStack(firstRecipeByOutput.getResultItem(null));
                maidCraftTask.setCacheRecipeId(path);
                maidCraftTask.targetCount = count;
                tasks.add(maidCraftTask);
                Recipe<?> output = RecipeUtils.getFirstRecipeByOutput(matchingStack, recipeType, event.getLevel());
                if (output != null){
                    NonNullList<Ingredient> ingredients = output.getIngredients();
                    matchingStack = ingredients.get(0).getItems()[0];
                }
            }
        }
        if (tasks.size() > 3) {
            tasks.subList(3, tasks.size()).clear();
        }
        Collections.reverse(tasks);
        tasks.get(tasks.size()-1).end = true;
        MaidTaskLinkedList maidTaskLinkedList = new MaidTaskLinkedList();
        maidTaskLinkedList.repeatCount = count;
        for (AbstractCraftMaidTask task : tasks) {
            maidTaskLinkedList.add(new MaidTaskNode(task));
        }
        if (maidTaskLinkedList.hand != null){
            BakingTasks.add(maid.getUUID(), maidTaskLinkedList);
        }
        BlockPos pos = maid.blockPosition();
        event.getLevel().playSound(null,pos,SoundEvents.EXPERIENCE_ORB_PICKUP,SoundSource.BLOCKS);
    }
}
