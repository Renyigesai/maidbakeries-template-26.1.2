package com.renyigesai.maid_bakeries.init;

import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.item.CraftOrderItem;
import com.renyigesai.maid_bakeries.item.CraftListItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MaidBakeriesItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(MaidBakeries.MODID);

    public static final DeferredItem<Item> CRAFT_LIST;
    public static final DeferredItem<Item> CRAFT_ORDER;

    static {
        CRAFT_LIST = REGISTER.register("craft_list", CraftListItem::new);
        CRAFT_ORDER = REGISTER.register("craft_order", CraftOrderItem::new);
    }
}
