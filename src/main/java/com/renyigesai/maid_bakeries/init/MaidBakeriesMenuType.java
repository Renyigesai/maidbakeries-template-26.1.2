package com.renyigesai.maid_bakeries.init;

import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.gui.CraftOrderMenu;
import com.renyigesai.maid_bakeries.gui.CraftOrderScreen;
import com.renyigesai.maid_bakeries.gui.CraftListMenu;
import com.renyigesai.maid_bakeries.gui.CraftListScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MaidBakeriesMenuType {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MaidBakeries.MODID);

    public static final RegistryObject<MenuType<CraftListMenu>> CRAFT_LIST_MENU = REGISTRY.register("craft_list_menu",
            () -> IForgeMenuType.create(CraftListMenu::create));
    public static final RegistryObject<MenuType<CraftOrderMenu>> CRAFT_ORDER_MENU = REGISTRY.register("craft_order_menu",
            () -> IForgeMenuType.create(CraftOrderMenu::create));

    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MaidBakeriesMenuType.CRAFT_LIST_MENU.get(), CraftListScreen::new);
            MenuScreens.register(MaidBakeriesMenuType.CRAFT_ORDER_MENU.get(), CraftOrderScreen::new);
        });
    }
}
