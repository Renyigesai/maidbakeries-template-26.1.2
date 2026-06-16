package com.renyigesai.maid_bakeries.network;

import com.renyigesai.maid_bakeries.MaidBakeries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record SyncRepeatPayload(ItemStack stack, int repeat) implements CustomPacketPayload {

    public static final Type<SyncRepeatPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MaidBakeries.MODID, "sync_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRepeatPayload> STREAM_CODEC =
            StreamCodec.composite(

                    ItemStack.STREAM_CODEC,
                    SyncRepeatPayload::stack,

                    ByteBufCodecs.VAR_INT,
                    SyncRepeatPayload::repeat,

                    SyncRepeatPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
