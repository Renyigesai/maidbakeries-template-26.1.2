package com.renyigesai.maid_bakeries.network;

import com.renyigesai.maid_bakeries.MaidBakeries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record SyncCheckoutPayload (List<ItemStack> stacks,String ids,ItemStack listStack) implements CustomPacketPayload{
    public static final CustomPacketPayload.Type<SyncCheckoutPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MaidBakeries.MODID, "sync_checkout"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCheckoutPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    SyncCheckoutPayload::stacks,
                    ByteBufCodecs.STRING_UTF8,
                    SyncCheckoutPayload::ids,
                    ItemStack.STREAM_CODEC,
                    SyncCheckoutPayload::listStack,
                    SyncCheckoutPayload::new
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
