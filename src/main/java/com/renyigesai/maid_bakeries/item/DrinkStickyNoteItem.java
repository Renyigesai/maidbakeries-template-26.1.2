package com.renyigesai.maid_bakeries.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DrinkStickyNoteItem extends Item {
    public DrinkStickyNoteItem(Properties p_41383_) {
        super(p_41383_);
    }

    public static final int OUTPUT = 4;
    public DrinkStickyNoteItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static List<ItemStack> getInventoryList(ItemStack stack){
        List<ItemStack> stacks = new ArrayList<>();
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("Inventory", Tag.TAG_COMPOUND)){
            ItemStackHandler handler = new ItemStackHandler(5);
            handler.deserializeNBT(tag.getCompound("Inventory"));
            for (int i = 0; i < handler.getSlots(); i++) {
                stacks.add(handler.getStackInSlot(i));
            }
        }
        return stacks;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        List<ItemStack> stacks = getInventoryList(pStack);
        if (!stacks.isEmpty()){
            for (int i = 0; i < stacks.size() - 1; i++) {
                if (!stacks.get(i).isEmpty()) {
                    pTooltip.add(Component.literal(stacks.get(i).getItem().getName(stacks.get(i)).getString()).withStyle(ChatFormatting.GOLD));
                }
            }
            pTooltip.add(Component.literal(stacks.get(OUTPUT).getItem().getName(stacks.get(OUTPUT)).getString()).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }
}
