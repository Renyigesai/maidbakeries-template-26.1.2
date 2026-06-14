package com.renyigesai.maid_bakeries.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StickyNotePageTurningMessage {
    private final int page;

    public StickyNotePageTurningMessage(FriendlyByteBuf buffer) {
        this.page = buffer.readInt();
    }

    public StickyNotePageTurningMessage(int page) {
        this.page = page;
    }

    public static void toBytes(StickyNotePageTurningMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.page);
    }

    public static void handle(StickyNotePageTurningMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player entity = context.getSender();
            int page = message.page;
            if (entity == null){
                return;
            }
            handleButtonAction(entity,page);
        });
        context.setPacketHandled(true);
    }

    public static void handleButtonAction(Player entity,int page) {
        ItemStack itemInHand = entity.getItemInHand(InteractionHand.MAIN_HAND);
        int oldPage = itemInHand.getOrCreateTag().getInt("Page");
        if (page < 1 && oldPage == 0){
            return;
        }
        itemInHand.getOrCreateTag().putInt("Page",oldPage + (page) );
    }
}
