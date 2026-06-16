package com.renyigesai.maid_bakeries.init;

import com.google.common.collect.ImmutableSet;
import com.renyigesai.bakeries.common.init.BakeriesBlocks;
import com.renyigesai.maid_bakeries.MaidBakeries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MaidBakeriesPoiTypes {

    public static final DeferredRegister<PoiType> POI_TYPE =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MaidBakeries.MODID);

    public static final Supplier<PoiType> OVEN_POI = POI_TYPE.register("oven_poi",
            ()-> new PoiType(ImmutableSet.copyOf(BakeriesBlocks.OVEN.get().getStateDefinition().getPossibleStates())
                    ,1,1));

    public static final Supplier<PoiType> BLENDER_POI = POI_TYPE.register("blender_poi",
            ()-> new PoiType(ImmutableSet.copyOf(BakeriesBlocks.BLENDER.get().getStateDefinition().getPossibleStates())
                    ,1,1));

    public static final Supplier<PoiType> CRAFTING_POI = POI_TYPE.register("crafting_poi",
            ()-> new PoiType(ImmutableSet.copyOf(Blocks.CRAFTING_TABLE.getStateDefinition().getPossibleStates())
                    ,1,1));

    public static final Supplier<PoiType> DOUGH_CRAFTING_TABLE_POI = POI_TYPE.register("dough_crafting_table_poi",
            ()-> new PoiType(ImmutableSet.copyOf(BakeriesBlocks.DOUGH_CRAFTING_TABLE.get().getStateDefinition().getPossibleStates())
                    ,1,1));

    public static void register(IEventBus eventBus){
        POI_TYPE.register(eventBus);
    }
}
