package com.renyigesai.maid_bakeries.network;

import com.renyigesai.maid_bakeries.data.IngredientData;
import com.renyigesai.maid_bakeries.data.RecipeCatalog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncRecipeCatalogMessage {
    private final List<List<IngredientData>> items;

    public SyncRecipeCatalogMessage(List<List<IngredientData>> items) {
        this.items = items;
    }

    public SyncRecipeCatalogMessage(FriendlyByteBuf buffer) {
        int outerSize = buffer.readInt();
        List<List<IngredientData>> items = new ArrayList<>(outerSize);
        for (int i = 0; i < outerSize; i++) {
            int innerSize = buffer.readInt();
            List<IngredientData> inner = new ArrayList<>(innerSize);
            for (int j = 0; j < innerSize; j++) {
                ItemStack stack = buffer.readItem();
                String type = buffer.readUtf();
                inner.add(new IngredientData(stack, type));
            }
            items.add(inner);
        }
        this.items = items;
    }

    public static void toBytes(SyncRecipeCatalogMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.items.size());
        for (List<IngredientData> list : message.items) {
            buffer.writeInt(list.size());
            for (IngredientData data : list) {
                buffer.writeItem(data.stack);
                buffer.writeUtf(data.type);
            }
        }
    }

    public static void handle(SyncRecipeCatalogMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            RecipeCatalog.items.clear();
            RecipeCatalog.items.addAll(message.items);
        });
        context.setPacketHandled(true);
    }
}