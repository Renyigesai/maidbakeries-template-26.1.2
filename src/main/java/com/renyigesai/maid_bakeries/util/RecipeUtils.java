package com.renyigesai.maid_bakeries.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeUtils {
    /**
     * 获取特定配方类型的所有成品
     * @param level 世界实例
     * @param recipeType 配方类型
     * @return 包含所有成品的集合
     */
    public static List<ItemStack> getAllRecipeResults(Level level, RecipeType<?> recipeType) {
        List<ItemStack> results = new ArrayList<>();
        RecipeManager recipeManager = level.getRecipeManager();
        Collection<Recipe<?>> recipes = recipeManager.getRecipes().stream()
                .filter(recipe -> recipe.getType() == recipeType)
                .toList();
        for (Recipe<?> recipe : recipes) {
            try {
                ItemStack result = recipe.getResultItem(level.registryAccess());
                if (!result.isEmpty()) {
                    results.add(result);
                }
            } catch (Exception e) {
                LogUtils.getLogger().warn("Failed to get result for recipe: {}", recipe.getId(), e);
            }
        }
        return results;
    }

    public static boolean isResultItem(Level level, RecipeType<?> recipeType,ItemStack outResult){
        List<ItemStack> allRecipeResults = getAllRecipeResults(level, recipeType);
        for (ItemStack allRecipeResult : allRecipeResults) {
            if (allRecipeResult.is(outResult.getItem())) {
                return true;
            }
        }
        return false;
    }

    public static List<Recipe<?>> getRecipesByOutput(ItemStack output, RecipeType<?> recipeType, Level level) {
        if (level == null || level.isClientSide) {
            return List.of();
        }
        List<Recipe<?>> matchedRecipes = new ArrayList<>();
        RecipeManager recipeManager = level.getRecipeManager();
        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            if (recipe.getType() == recipeType) {
                ItemStack result = recipe.getResultItem(level.registryAccess());
                if (result.is(output.getItem())) {
                    matchedRecipes.add(recipe);
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
        return recipes.isEmpty() ? null : recipes.get(0);
    }

    public static List<ItemStack> getIngredientsFromRecipe(Recipe<?> recipe, Level level) {
        if (recipe == null) {
            return List.of();
        }
        List<ItemStack> ingredients = new ArrayList<>();
        for (Ingredient ing : recipe.getIngredients()) {
            if (ing.isEmpty()) {
                continue;
            }
            ItemStack[] stacks = ing.getItems();
            if (stacks.length > 0) {
//                for (int i = 0; i < stacks.length; i++) {
//                    System.out.println(stacks[i]);
//                }
                ingredients.add(stacks[0].copy());
            }
        }
        return ingredients;
    }

    public static List<ItemStack> getIngredientsByOutput(ItemStack output, RecipeType<?> recipeType, Level level) {
        if (level == null || level.isClientSide) {
            return List.of(); // 客户端配方数据不完整
        }
        Recipe<?> recipe = getFirstRecipeByOutput(output, recipeType, level);
        return getIngredientsFromRecipe(recipe, level);
    }

    public static List<ItemStack> getBakeriesItem(){
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);
            if (registryName != null && registryName.getNamespace().equals("bakeries")) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks;
    }

    public static List<ItemStack> getMatchList(NonNullList<Ingredient> ingredients, IItemHandler itemHandler) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            if (ingredient == Ingredient.EMPTY) {
                continue;
            }
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                ItemStack maidItem = itemHandler.getStackInSlot(slot);
                if (ingredient.test(maidItem)) {
                    stacks.add(maidItem);
                    break;
                }
            }
        }
        return stacks;
    }
}