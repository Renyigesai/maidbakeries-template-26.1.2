package com.renyigesai.maid_bakeries.init;

import com.renyigesai.maid_bakeries.MaidBakeries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;

public class MaidBakeriesTags {

    public static final TagKey<PoiType> OVEN = createPoiTypeTag("oven");
    public static final TagKey<PoiType> BLENDER = createPoiTypeTag("blender");
    public static final TagKey<PoiType> CRAFTING_TABLE = createPoiTypeTag("crafting_table");
    public static final TagKey<PoiType> DOUGH_CRAFTING_TABLE = createPoiTypeTag("dough_crafting_table");

    private static TagKey<PoiType> createPoiTypeTag(String pName) {
        return TagKey.create(Registries.POINT_OF_INTEREST_TYPE, MaidBakeries.prefix(pName));
    }

}
