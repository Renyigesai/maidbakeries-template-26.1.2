package com.renyigesai.maid_bakeries.network;

import com.renyigesai.maid_bakeries.MaidBakeries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record SyncCraftListAddPayload(ItemStack stack, ItemStack listItem,String ids) implements CustomPacketPayload{
    public static final Type<SyncCraftListAddPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MaidBakeries.MODID, "sync_craft_list_add"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCraftListAddPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    SyncCraftListAddPayload::stack,
                    ItemStack.STREAM_CODEC,
                    SyncCraftListAddPayload::listItem,
                    ByteBufCodecs.STRING_UTF8,
                    SyncCraftListAddPayload::ids,
                    SyncCraftListAddPayload::new
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
