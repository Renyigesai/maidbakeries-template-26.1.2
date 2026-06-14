package com.renyigesai.maid_bakeries.data;

import net.minecraft.resources.ResourceLocation;
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
}
