package com.renyigesai.maid_bakeries.gui;

import com.renyigesai.maid_bakeries.init.MaidBakeriesMenuType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class CraftOrderMenu extends AbstractContainerMenu {
    private final IItemHandler playerInventory;
    public CraftOrderMenu(int w, Inventory playerInventory) {
        super(MaidBakeriesMenuType.CRAFT_ORDER_MENU.get(), w);
        this.playerInventory = new InvWrapper(playerInventory);
    }

    protected void addPlayerSlots() {
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
            this.addSlot(new SlotItemHandler(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 84 + 58));
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new SlotItemHandler(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static CraftOrderMenu create(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        return new CraftOrderMenu(windowId,playerInventory);
    }
}
