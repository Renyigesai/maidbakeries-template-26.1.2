package com.renyigesai.maid_bakeries.item;

import com.renyigesai.maid_bakeries.data.RecipeCatalog;
import com.renyigesai.maid_bakeries.gui.CraftListMenu;
import com.renyigesai.maid_bakeries.init.MaidBakeriesDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.List;

public class CraftListItem extends Item {
    public CraftListItem(Identifier identifier) {
        super(new Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY).component(MaidBakeriesDataComponents.TEXT,"").setId(ResourceKey.create(Registries.ITEM,identifier)));
    }

    public static List<ItemStack> getInventoryList(ItemStack stack){
        ItemContainerContents itemContainerContents = stack.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY);
        return itemContainerContents.allItemsCopyStream().toList();
    }

    public static void clear(ItemStack stack){
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY)));
        stack.set(MaidBakeriesDataComponents.TEXT, "");
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.is(this)) {
            player.startUsingItem(hand);
            player.openMenu(getMenuProvider());
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }

    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider((p_53812_, p_53813_, p_53814_) -> new CraftListMenu(p_53812_,p_53813_), Component.nullToEmpty("Craft List"));
    }
}
