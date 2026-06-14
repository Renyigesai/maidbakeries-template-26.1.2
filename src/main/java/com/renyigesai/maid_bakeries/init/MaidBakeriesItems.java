package com.renyigesai.maid_bakeries.init;

import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.item.CraftOrderItem;
import com.renyigesai.maid_bakeries.item.DrinkStickyNoteItem;
import com.renyigesai.maid_bakeries.item.OvenStickyNoteItem;
import com.renyigesai.maid_bakeries.item.CraftListItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MaidBakeriesItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MaidBakeries.MODID);

    public static final RegistryObject<Item> OVEN_STICKY_NOTE;
    public static final RegistryObject<Item> DRINK_STICKY_NOTE;
    public static final RegistryObject<Item> CRAFT_LIST;
    public static final RegistryObject<Item> CRAFT_ORDER;

    static {
        OVEN_STICKY_NOTE = REGISTER.register("oven_sticky_note", OvenStickyNoteItem::new);
        DRINK_STICKY_NOTE = REGISTER.register("drink_sticky_note", DrinkStickyNoteItem::new);
        CRAFT_LIST = REGISTER.register("craft_list", CraftListItem::new);
        CRAFT_ORDER = REGISTER.register("craft_order", ()-> new CraftOrderItem(new Item.Properties()));
    }
}
