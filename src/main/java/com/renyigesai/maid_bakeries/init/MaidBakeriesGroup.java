package com.renyigesai.maid_bakeries.init;

import com.renyigesai.bakeries.BakeriesMod;
import com.renyigesai.maid_bakeries.MaidBakeries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MaidBakeriesGroup {
    private static final ResourceLocation CREATIVE_INVENTORY_TABS_IMAGE = ResourceLocation.fromNamespaceAndPath(MaidBakeries.MODID,"textures/gui/container/creative_inventory/tabs.png");

    public static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BakeriesMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAID_BAKERY_TAB = REGISTER.register("maid_bakeries_tab",() ->
            CreativeModeTab.builder().icon(()-> new ItemStack(MaidBakeriesItems.CRAFT_LIST.get()))
                    .title(Component.translatable("creativetab_maid_bakeries_tab"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(MaidBakeriesItems.CRAFT_LIST.get());
                        output.accept(MaidBakeriesItems.CRAFT_ORDER.get());
                    }))
                    .build());
}
