package com.renyigesai.maid_bakeries;

import com.renyigesai.maid_bakeries.init.*;
import com.renyigesai.maid_bakeries.network.Messages;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MaidBakeries.MODID)
public class MaidBakeries {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "maid_bakeries";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public MaidBakeries() {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MaidBakeriesItems.REGISTER.register(bus);
        MaidBakeriesGroup.REGISTER.register(bus);
        MaidBakeriesMenuType.REGISTRY.register(bus);
        bus.addListener(this::commonSetup);
        MaidBakeriesPoiTypes.register(bus);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Messages.register();
    }

    public static ResourceLocation prefix(String name) {
        return new ResourceLocation(MODID, name.toLowerCase(Locale.ROOT));
    }
}
