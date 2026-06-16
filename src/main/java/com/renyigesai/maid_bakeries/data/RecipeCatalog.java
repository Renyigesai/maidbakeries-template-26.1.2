package com.renyigesai.maid_bakeries.data;

import com.renyigesai.bakeries.common.init.BakeriesRecipes;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.entity.task.AbstractCraftMaidTask;
import com.renyigesai.maid_bakeries.entity.task.impl.MaidCraftBlenderTask;
import com.renyigesai.maid_bakeries.entity.task.impl.MaidCraftDoughCraftingTableTask;
import com.renyigesai.maid_bakeries.entity.task.impl.MaidCraftOvenTask;
import com.renyigesai.maid_bakeries.entity.task.impl.MaidCraftingTask;
import com.renyigesai.maid_bakeries.util.IORecipeAccessor;
import com.renyigesai.maid_bakeries.util.RecipeUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.*;
import java.util.function.Supplier;

public class RecipeCatalog {
    public static final Map<String,RecipeType<?>> RECIPE_TYPES;
    public static final Map<String, Supplier<AbstractCraftMaidTask>> TASKS;

    static {
        RECIPE_TYPES = Map.of("oven",BakeriesRecipes.OVEN_TYPE.get(), "dough_crafting_table",BakeriesRecipes.DOUGH_CRAFTING_TABLE_TYPE.get(), "blender",BakeriesRecipes.BLENDER_TYPE.get());
        TASKS = Map.of("oven",MaidCraftOvenTask::new,"dough_crafting_table",MaidCraftDoughCraftingTableTask::new,"blender",MaidCraftBlenderTask::new);
    }

}
