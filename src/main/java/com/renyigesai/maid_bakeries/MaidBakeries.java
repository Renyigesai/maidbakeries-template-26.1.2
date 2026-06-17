package com.renyigesai.maid_bakeries;

import com.renyigesai.bakeries.common.init.BakeriesRecipes;
import com.renyigesai.maid_bakeries.init.*;
import com.renyigesai.maid_bakeries.util.IORecipeAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.Objects;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MaidBakeries.MODID)
public class MaidBakeries {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "maid_bakeries";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public MaidBakeries(IEventBus modEventBus) {
        MaidBakeriesDataComponents.REGISTER.register(modEventBus);
        MaidBakeriesItems.REGISTER.register(modEventBus);
        MaidBakeriesGroup.REGISTER.register(modEventBus);
        MaidBakeriesMenuType.REGISTRY.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        MaidBakeriesPoiTypes.register(modEventBus);
    }

    private static void registerIORecipeAccessor(){
        /*注册搅拌机配方的输入输出*/
        IORecipeAccessor.registerInput(BakeriesRecipes.BLENDER_TYPE.get(), recipe -> recipe.getInputItems());
        IORecipeAccessor.registerOutput(BakeriesRecipes.BLENDER_TYPE.get(), recipe -> recipe.result().create());

        /*注册烤炉配方的输入输出*/
        IORecipeAccessor.registerInput(BakeriesRecipes.OVEN_TYPE.get(), recipe -> {
            NonNullList<Ingredient> list = NonNullList.create();
            list.add(recipe.getInput());
            return list;
        });
        IORecipeAccessor.registerOutput(BakeriesRecipes.OVEN_TYPE.get(), recipe -> recipe.getResult().create());

        /*注册面胚制作台配方的输入输出*/
        IORecipeAccessor.registerInput(BakeriesRecipes.DOUGH_CRAFTING_TABLE_TYPE.get(), recipe -> {
            NonNullList<Ingredient> list = NonNullList.create();
            list.add(recipe.input());
            return list;
        });
        IORecipeAccessor.registerOutput(BakeriesRecipes.DOUGH_CRAFTING_TABLE_TYPE.get(),  recipe -> recipe.result.create());

        /*注册工作台无序配方的输入输出*/
        IORecipeAccessor.registerInput(RecipeType.CRAFTING, recipe -> {
            if (recipe instanceof ShapelessRecipe shapelessRecipe){
                return NonNullList.copyOf(shapelessRecipe.ingredients);
            }
            return NonNullList.create();
        });

        IORecipeAccessor.registerOutput(RecipeType.CRAFTING, recipe -> {
            if (recipe instanceof ShapelessRecipe shapelessRecipe){
                if (shapelessRecipe.result() != null) {
                    return shapelessRecipe.result().create();
                }
            }
            return ItemStack.EMPTY;
        });

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        registerIORecipeAccessor();
    }

    public static Identifier prefix(String name) {
        return Identifier.fromNamespaceAndPath(MODID, name.toLowerCase(Locale.ROOT));
    }
}
