package com.renyigesai.maid_bakeries.util;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.*;

public class RecipeUtils {
    /**
     * 获取特定配方类型的所有成品
     * @param level 世界实例
     * @param recipeType 配方类型
     * @return 包含所有成品的集合
     */
    public static <I extends RecipeInput, T extends Recipe<I>> List<ItemStack> getAllRecipeResults(Level level, RecipeType<T> recipeType) {
        List<ItemStack> results = new ArrayList<>();
        if (level.recipeAccess() instanceof RecipeManager recipeManager) {
            recipeManager.recipeMap().byType(recipeType).forEach(holder -> {
                results.add(IORecipeAccessor.getOutput(holder.value()));
            });
        }
        return results;
    }

    public static boolean isResultItem(Level level, RecipeType<?> recipeType, ItemStack outResult) {
        if (level.recipeAccess() instanceof RecipeManager manager) {
            // 遍历所有配方，筛选出指定类型
            for (RecipeHolder<?> holder : manager.getRecipes()) {
                if (holder.value().getType() == recipeType) {
                    ItemStack output = IORecipeAccessor.getOutput(holder.value());
                    if (ItemStack.isSameItemSameComponents(output, outResult)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static List<Recipe<?>> getRecipesByOutput(ItemStack output, RecipeType<?> recipeType, Level level) {
        if (level == null || level.isClientSide()) {
            return List.of();
        }
        List<Recipe<?>> matchedRecipes = new ArrayList<>();
        if (level.recipeAccess() instanceof RecipeManager recipeManager){
            for (RecipeHolder<?> recipe : recipeManager.getRecipes()) {
                if (recipe.value().getType() == recipeType) {
                    ItemStack result = IORecipeAccessor.getOutput(recipe.value());
                    if (result.is(output.getItem())) {
                        matchedRecipes.add(recipe.value());
                    }
                }
            }
        }
        return matchedRecipes;
    }

    public static List<RecipeHolder<?>> getRecipeHoldersByOutput(ItemStack output, RecipeType<?> recipeType, Level level) {
        if (level == null || level.isClientSide()) {
            return List.of();
        }
        List<RecipeHolder<?>> matchedRecipes = new ArrayList<>();
        if (level.recipeAccess() instanceof RecipeManager recipeManager){
            for (RecipeHolder<?> recipe : recipeManager.getRecipes()) {
                if (recipe.value().getType() == recipeType) {
                    ItemStack result = IORecipeAccessor.getOutput(recipe.value());
                    if (result.is(output.getItem())) {
                        matchedRecipes.add(recipe);
                    }
                }
            }
        }
        return matchedRecipes;
    }

    /**
     * 获取第一个匹配的配方（如果有多个，只返回第一个）。
     * @see #getRecipesByOutput(ItemStack, RecipeType, Level)
     */
    public static Recipe<?> getFirstRecipeByOutput(ItemStack output, RecipeType<?> recipeType, Level level) {
        List<Recipe<?>> recipes = getRecipesByOutput(output, recipeType, level);
        return recipes.isEmpty() ? null : recipes.getFirst();
    }

    public static RecipeHolder<?> getFirstRecipeHolderByOutput(ItemStack output, RecipeType<?> recipeType, Level level) {
        List<RecipeHolder<?>> recipes = getRecipeHoldersByOutput(output, recipeType, level);
        return recipes.isEmpty() ? null : recipes.getFirst();
    }

    public static List<ItemStack> getIngredientsFromRecipe(Recipe<?> recipe, Level level) {
        if (recipe == null) {
            return List.of();
        }
        List<ItemStack> ingredients = new ArrayList<>();
        for (Ingredient ing : IORecipeAccessor.getInput(recipe)) {
            if (ing.isEmpty()) {
                continue;
            }
            List<Holder<Item>> list = ing.getValues().stream().toList();
            ItemStack[] stacks = new ItemStack[list.size()];
            for (int i = 0; i < list.size(); i++) {
                stacks[i] = new ItemStack(list.get(i));
            }
            if (stacks.length > 0) {
                ingredients.add(stacks[0].copy());
            }
        }
        return ingredients;
    }

    public static List<ItemStack> getBakeriesItem(){
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM.stream().toList()) {
            Identifier registryName =BuiltInRegistries.ITEM.getKey(item);
            if (registryName != null && registryName.getNamespace().equals("bakeries")) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks;
    }

    public static List<ItemResource> getMatchList(NonNullList<Ingredient> ingredients, CombinedResourceHandler<ItemResource> itemHandler) {
        List<ItemResource> stacks = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.test(ItemStack.EMPTY)) {
                continue;
            }
            for (int slot = 0; slot < itemHandler.size(); slot++) {
                ItemResource maidItem = itemHandler.getResource(slot);
                if (ingredient.test(maidItem.toStack())) {
                    stacks.add(maidItem);
                    break;
                }
            }
        }
        return stacks;
    }
    public static List<Integer> getMatchListIndex(NonNullList<Ingredient> ingredients, CombinedResourceHandler<ItemResource> itemHandler) {
        List<Integer> stacks = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.test(ItemStack.EMPTY)) {
                continue;
            }
            for (int slot = 0; slot < itemHandler.size(); slot++) {
                if (ingredient.test(itemHandler.getResource(slot).toStack())) {
                    stacks.add(slot);
                    break;
                }
            }
        }
        return stacks;
    }
}