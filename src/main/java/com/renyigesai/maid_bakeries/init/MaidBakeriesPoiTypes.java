package com.renyigesai.maid_bakeries.init;

import com.google.common.collect.ImmutableSet;
import com.renyigesai.bakeries.BakeriesMod;
import com.renyigesai.bakeries.init.BakeriesBlocks;
import com.renyigesai.maid_bakeries.MaidBakeries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MaidBakeriesPoiTypes {

    public static final DeferredRegister<PoiType> POI_TYPE =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, MaidBakeries.MODID);

    public static final RegistryObject<PoiType> OVEN_POI = POI_TYPE.register("oven_poi",
            ()-> new PoiType(ImmutableSet.copyOf(BakeriesBlocks.OVEN.get().getStateDefinition().getPossibleStates())
                    ,1,1));

    public static final RegistryObject<PoiType> BLENDER_POI = POI_TYPE.register("blender_poi",
            ()-> new PoiType(ImmutableSet.copyOf(BakeriesBlocks.BLENDER.get().getStateDefinition().getPossibleStates())
                    ,1,1));

    public static final RegistryObject<PoiType> CRAFTING_POI = POI_TYPE.register("crafting_poi",
            ()-> new PoiType(ImmutableSet.copyOf(Blocks.CRAFTING_TABLE.getStateDefinition().getPossibleStates())
                    ,1,1));

    public static void register(IEventBus eventBus){
        POI_TYPE.register(eventBus);
    }
}
