package com.renyigesai.maid_bakeries.network;

import com.renyigesai.bakeries.util.ItemUtils;
import com.renyigesai.maid_bakeries.init.MaidBakeriesItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CheckoutMessage {
    private final CompoundTag stacks;
    private final String ids;

    public CheckoutMessage(FriendlyByteBuf buffer) {
        this.stacks = buffer.readNbt();
        this.ids = buffer.readUtf();
    }

    public CheckoutMessage(CompoundTag stacks, String ids) {
        this.stacks = stacks;
        this.ids = ids;
    }

    public static void toBytes(CheckoutMessage message, FriendlyByteBuf buffer) {
        buffer.writeNbt(message.stacks);
        buffer.writeUtf(message.ids);
    }

    public static void handle(CheckoutMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player entity = context.getSender();
            CompoundTag stacks = message.stacks;
            if (entity == null){
                return;
            }
            handleButtonAction(entity,stacks, message.ids);
        });
        context.setPacketHandled(true);
    }

    public static void handleButtonAction(Player entity,CompoundTag stack,String type) {
        ItemStack stickyNote = new ItemStack(MaidBakeriesItems.CRAFT_ORDER.get());
        CompoundTag orCreateTag = stickyNote.getOrCreateTag();
        orCreateTag.put("Inventory",stack);
        orCreateTag.putString("Type",type);
        orCreateTag.putInt("Repeat",1);
        stickyNote.setTag(orCreateTag);
        ItemUtils.givePlayerItem(entity,stickyNote);
    }
}
