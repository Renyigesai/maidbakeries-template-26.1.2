package com.renyigesai.maid_bakeries.init;

import com.renyigesai.maid_bakeries.MaidBakeries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class MaidBakeriesGroup {

    public static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MaidBakeries.MODID);

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> MAID_BAKERY_TAB = REGISTER.register("maid_bakeries_tab",() ->
            CreativeModeTab.builder().icon(()-> new ItemStack(MaidBakeriesItems.CRAFT_LIST.get()))
                    .title(Component.translatable("creativetab_maid_bakeries_tab"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(MaidBakeriesItems.CRAFT_LIST.get());
                        output.accept(MaidBakeriesItems.CRAFT_ORDER.get());
                    }))
                    .build());
}
