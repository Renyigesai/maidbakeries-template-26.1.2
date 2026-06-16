package com.renyigesai.maid_bakeries;

import com.renyigesai.bakeries.common.init.BakeriesRecipes;
import com.renyigesai.maid_bakeries.init.*;
import com.renyigesai.maid_bakeries.util.IORecipeAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

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

    private static void registerMBRecipeAccessor(){
        IORecipeAccessor.registerInput(BakeriesRecipes.BLENDER_TYPE.get(), recipe -> recipe.getInputItems());
        IORecipeAccessor.registerOutput(BakeriesRecipes.BLENDER_TYPE.get(), recipe -> recipe.result().create());

        IORecipeAccessor.registerInput(BakeriesRecipes.OVEN_TYPE.get(), recipe -> NonNullList.of(recipe.getInput()));
        IORecipeAccessor.registerOutput(BakeriesRecipes.OVEN_TYPE.get(), recipe -> recipe.getResult().create());

        IORecipeAccessor.registerInput(BakeriesRecipes.DOUGH_CRAFTING_TABLE_TYPE.get(), recipe -> NonNullList.of(recipe.input()));
        IORecipeAccessor.registerOutput(BakeriesRecipes.DOUGH_CRAFTING_TABLE_TYPE.get(),  recipe -> recipe.result.create());

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        registerMBRecipeAccessor();
    }

    public static Identifier prefix(String name) {
        return Identifier.fromNamespaceAndPath(MODID, name.toLowerCase(Locale.ROOT));
    }
}
