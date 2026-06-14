package com.renyigesai.maid_bakeries.data;

import com.renyigesai.bakeries.init.BakeriesItems;
import com.renyigesai.bakeries.recipe.BlenderRecipe;
import com.renyigesai.bakeries.recipe.DoughCraftingRecipe;
import com.renyigesai.bakeries.recipe.oven.OvenRecipe;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.entity.task.impl.MaidCraftBlenderTask;
import com.renyigesai.maid_bakeries.entity.task.impl.MaidCraftDoughCraftingTableTask;
import com.renyigesai.maid_bakeries.entity.task.impl.MaidCraftOvenTask;
import com.renyigesai.maid_bakeries.entity.task.impl.MaidCraftingTask;
import com.renyigesai.maid_bakeries.util.RecipeUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.*;

public class RecipeCatalog {
    public static final List<List<IngredientData>> items;
    public static final List<RecipeType<?>> RECIPE_TYPES = List.of(OvenRecipe.Type.INSTANCE, DoughCraftingRecipe.Type.INSTANCE, BlenderRecipe.Type.INSTANCE,RecipeType.CRAFTING);
    public static final Map<RecipeType<?>, AbstractCraftMaidTask> TASKS = Map.of(OvenRecipe.Type.INSTANCE,new MaidCraftOvenTask(),DoughCraftingRecipe.Type.INSTANCE,new MaidCraftDoughCraftingTableTask(),BlenderRecipe.Type.INSTANCE,new MaidCraftBlenderTask(),RecipeType.CRAFTING,new MaidCraftingTask());
    public static final Map<RecipeType<?>,String> RECIPE_TYPES_ID = Map.of(OvenRecipe.Type.INSTANCE,"oven",DoughCraftingRecipe.Type.INSTANCE,"dough_crafting_table",BlenderRecipe.Type.INSTANCE,"blender",RecipeType.CRAFTING,"crafting");

    public static void init(ServerLevel level){
        if (!items.isEmpty()){
            return;
        }
        List<ItemStack> results = RecipeUtils.getBakeriesItem();
        for (ItemStack resultsItem : results) {
            List<IngredientData> stacks = new ArrayList<>(3);
            ItemStack matchingStack = resultsItem.copy();
            for (RecipeType<?> type : RECIPE_TYPES) {
                if (RecipeUtils.isResultItem(level, type, matchingStack)) {
                    if (stacks.size() < 3){
                        IngredientData ingredientData = new IngredientData(matchingStack.copy());
                        ingredientData.type = RECIPE_TYPES_ID.get(type);
                        stacks.add(ingredientData);
                        Recipe<?> output = RecipeUtils.getFirstRecipeByOutput(matchingStack, type,level);
                        if (output != null){
                            NonNullList<Ingredient> ingredients = output.getIngredients();
                            if (ingredients.get(0).getItems().length > 0){
                                matchingStack = ingredients.get(0).getItems()[0].copy();
                            }
                        }
                    }
                }
            }
            if (!stacks.isEmpty()) {
                Collections.reverse(stacks);
                items.add(stacks);
            }
        }
        items.sort(Comparator
                .<List<IngredientData>>comparingInt(List::size)
                .reversed()
                .thenComparing(subList -> {
                    ItemStack first = subList.get(0).stack;
                    ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(first.getItem());
                    return registryName.toString();
                })
        );
        MaidBakeries.LOGGER.info("{} init!", RecipeCatalog.class);
    }
    static {
        items = new ArrayList<>();
    }
}
