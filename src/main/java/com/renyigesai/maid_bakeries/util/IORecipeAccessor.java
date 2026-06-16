package com.renyigesai.maid_bakeries.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class IORecipeAccessor {
    private static final Map<RecipeType<?>, Function<Recipe<?>, ItemStack>> OUTPUT_EXTRACTORS = new HashMap<>();
    private static final Map<RecipeType<?>, Function<Recipe<?>, NonNullList<Ingredient>>> INPUT_EXTRACTORS = new HashMap<>();

    public static <T extends Recipe<?>> void registerOutput(RecipeType<T> type, Function<T, ItemStack> extractor) {
        OUTPUT_EXTRACTORS.put(type, recipe -> extractor.apply((T) recipe));
    }

    public static <T extends Recipe<?>> void registerInput(RecipeType<T> type, Function<T, NonNullList<Ingredient>> extractor) {
        INPUT_EXTRACTORS.put(type, recipe -> extractor.apply((T) recipe));
    }

    // 获取配方的输出物品
    public static ItemStack getOutput(Recipe<?> recipe) {
        Function<Recipe<?>, ItemStack> extractor = OUTPUT_EXTRACTORS.get(recipe.getType());
        return extractor != null ? extractor.apply(recipe) : ItemStack.EMPTY;
    }

    public static NonNullList<Ingredient> getInput(Recipe<?> recipe) {
        Function<Recipe<?>, NonNullList<Ingredient>> extractor = INPUT_EXTRACTORS.get(recipe.getType());
        return extractor != null ? extractor.apply(recipe) : NonNullList.create();
    }
}
