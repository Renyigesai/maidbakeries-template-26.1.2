package com.renyigesai.maid_bakeries.item;

import com.renyigesai.maid_bakeries.gui.CraftOrderMenu;
import com.renyigesai.maid_bakeries.init.MaidBakeriesDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CraftOrderItem extends Item {
    public  CraftOrderItem(Identifier identifier) {
        super(new Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY).component(MaidBakeriesDataComponents.TEXT.get(),"").setId(ResourceKey.create(Registries.ITEM,identifier)));
    }
    public static List<ItemStack> getInventoryList(ItemStack stack){
        ItemContainerContents itemContainerContents = stack.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY);
        return itemContainerContents.allItemsCopyStream().toList();
    }

    public static ItemStack getEnd(ItemStack stack){
        List<ItemStack> inventoryList = getInventoryList(stack);
        if (!inventoryList.isEmpty()){
            return inventoryList.getLast();
        }
        return ItemStack.EMPTY;
    }

    public static List<String> getTypes(ItemStack stack) {
        if (stack.has(MaidBakeriesDataComponents.TEXT.get())) {
            String input = stack.get(MaidBakeriesDataComponents.TEXT.get());
            if (input == null || input.isEmpty()) {
                return List.of();
            }
            // 按 & 分割，并过滤掉空字符串
            return Arrays.stream(input.split("&"))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public static int getRepeat(ItemStack stack){
        return stack.getOrDefault(MaidBakeriesDataComponents.INT,0);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.is(this) && !getEnd(itemInHand).isEmpty()) {
            player.startUsingItem(hand);
            player.openMenu(getMenuProvider());
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }

    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider((p_53812_, p_53813_, p_53814_) -> new CraftOrderMenu(p_53812_,p_53813_), Component.nullToEmpty("Craft Order"));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        ItemStack end = getEnd(itemStack);
        if (!end.isEmpty()){
            builder.accept(Component.literal("Result:").withStyle(ChatFormatting.DARK_GRAY));
            builder.accept(Component.translatable(end.getItem().getDescriptionId()).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}
