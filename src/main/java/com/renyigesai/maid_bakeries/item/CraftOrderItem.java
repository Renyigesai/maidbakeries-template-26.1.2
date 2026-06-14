package com.renyigesai.maid_bakeries.item;

import com.renyigesai.maid_bakeries.gui.CraftOrderMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CraftOrderItem extends Item {
    public  CraftOrderItem(Properties p_41383_) {
        super(p_41383_);
    }
    public static List<ItemStack> getInventoryList(ItemStack stack){
        List<ItemStack> stacks = new ArrayList<>();
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("Inventory", Tag.TAG_COMPOUND)){
            ItemStackHandler handler = new ItemStackHandler(3);
            handler.deserializeNBT(tag.getCompound("Inventory"));
            for (int i = 0; i < handler.getSlots(); i++) {
                stacks.add(handler.getStackInSlot(i));
            }
        }
        return stacks;
    }

    public static ItemStack getEnd(ItemStack stack){
        List<ItemStack> inventoryList = getInventoryList(stack);
        if (!inventoryList.isEmpty()){
            return inventoryList.get(inventoryList.size()-1);
        }
        return ItemStack.EMPTY;
    }

    public static List<String> getTypes(ItemStack stack){
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("Type", Tag.TAG_STRING)){
            String input = tag.getString("Type");
            if (input.isEmpty()) {
                return List.of();
            }
            String[] parts = input.split("&");
            return Arrays.asList(parts);
        }
        return List.of();
    }

    public static int getRepeat(ItemStack stack){
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("Repeat", Tag.TAG_INT)){
            return tag.getInt("Repeat");
        }
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.is(this) && !getEnd(itemInHand).isEmpty()) {
            player.startUsingItem(hand);
            player.openMenu(getMenuProvider());
            return new InteractionResultHolder(InteractionResult.SUCCESS, player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider((p_53812_, p_53813_, p_53814_) -> new CraftOrderMenu(p_53812_,p_53813_), Component.nullToEmpty("Craft Order"));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        ItemStack end = getEnd(pStack);
        if (!end.isEmpty()){
            pTooltip.add(Component.literal("Result:").withStyle(ChatFormatting.DARK_GRAY));
            pTooltip.add(Component.translatable(end.getItem().getDescriptionId()).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }
}
