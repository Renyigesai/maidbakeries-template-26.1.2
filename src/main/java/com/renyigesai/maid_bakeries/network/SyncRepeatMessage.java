package com.renyigesai.maid_bakeries.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncRepeatMessage {
    private final int repeat;

    public SyncRepeatMessage(FriendlyByteBuf buffer) {
        this.repeat = buffer.readInt();
    }

    public SyncRepeatMessage(int page) {
        this.repeat = page;
    }

    public static void toBytes(SyncRepeatMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.repeat);
    }

    public static void handle(SyncRepeatMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player entity = context.getSender();
            int repeat = message.repeat;
            if (entity == null){
                return;
            }
            handleButtonAction(entity,repeat);
        });
        context.setPacketHandled(true);
    }

    public static void handleButtonAction(Player entity,int repeat) {
        ItemStack itemInHand = entity.getItemInHand(InteractionHand.MAIN_HAND);
        int oldRepeat = itemInHand.getOrCreateTag().getInt("Repeat");
        if (repeat == 64 || repeat < 1 && oldRepeat == 1){
            return;
        }
        itemInHand.getOrCreateTag().putInt("Repeat",oldRepeat + (repeat) );
    }
}
