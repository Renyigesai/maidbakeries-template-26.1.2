package com.renyigesai.maid_bakeries.gui;

import com.renyigesai.maid_bakeries.init.MaidBakeriesMenuType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

@SuppressWarnings("removal")
public class CraftListMenu extends AbstractContainerMenu {
    private final IItemHandler playerInventory;
    public CraftListMenu(int w, Inventory playerInventory) {
        super(MaidBakeriesMenuType.CRAFT_LIST_MENU.get(), w);
        this.playerInventory = new InvWrapper(playerInventory);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static CraftListMenu create(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        return new CraftListMenu(windowId,playerInventory);
    }
}
