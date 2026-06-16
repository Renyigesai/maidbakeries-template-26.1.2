package com.renyigesai.maid_bakeries.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class IngredientData {
    public final ItemStack stack;
    public String type = "not";

    public IngredientData(ItemStack stack) {
        this.stack = stack;
    }

    public IngredientData(ItemStack stack, String type) {
        this.stack = stack;
        this.type = type;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientData> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    d -> d.stack,
                    ByteBufCodecs.STRING_UTF8,
                    d -> d.type,
                    IngredientData::new
            );
}
