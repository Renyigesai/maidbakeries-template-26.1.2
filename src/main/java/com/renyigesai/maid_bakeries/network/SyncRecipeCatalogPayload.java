package com.renyigesai.maid_bakeries.network;

import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.data.IngredientData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record SyncRecipeCatalogPayload(List<List<IngredientData>> items) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncRecipeCatalogPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MaidBakeries.MODID, "sync_recipe_catalog"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRecipeCatalogPayload> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(
                            (buf, list) -> {
                                buf.writeVarInt(list.size());
                                for (List<IngredientData> inner : list) {
                                    buf.writeVarInt(inner.size());
                                    for (IngredientData data : inner) {
                                        IngredientData.STREAM_CODEC.encode(buf, data);
                                    }
                                }
                            },
                            buf -> {
                                int outerSize = buf.readVarInt();
                                List<List<IngredientData>> outer = new ArrayList<>(outerSize);
                                for (int i = 0; i < outerSize; i++) {
                                    int innerSize = buf.readVarInt();
                                    List<IngredientData> inner = new ArrayList<>(innerSize);
                                    for (int j = 0; j < innerSize; j++) {
                                        inner.add(IngredientData.STREAM_CODEC.decode(buf));
                                    }
                                    outer.add(inner);
                                }
                                return outer;
                            }
                    ),
                    SyncRecipeCatalogPayload::items,
                    SyncRecipeCatalogPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}