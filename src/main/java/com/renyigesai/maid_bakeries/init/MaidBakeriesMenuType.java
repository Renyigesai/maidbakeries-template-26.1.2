package com.renyigesai.maid_bakeries.init;

import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.gui.CraftOrderMenu;
import com.renyigesai.maid_bakeries.gui.CraftOrderScreen;
import com.renyigesai.maid_bakeries.gui.CraftListMenu;
import com.renyigesai.maid_bakeries.gui.CraftListScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@EventBusSubscriber(value = Dist.CLIENT)
public class MaidBakeriesMenuType {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, MaidBakeries.MODID);

    public static final Supplier<MenuType<CraftListMenu>> CRAFT_LIST_MENU = REGISTRY.register("craft_list_menu",
            () -> IMenuTypeExtension.create(CraftListMenu::create));
    public static final Supplier<MenuType<CraftOrderMenu>> CRAFT_ORDER_MENU = REGISTRY.register("craft_order_menu",
            () -> IMenuTypeExtension.create(CraftOrderMenu::create));

    @SubscribeEvent
    public static void clientLoad(RegisterMenuScreensEvent event) {
        event.register(MaidBakeriesMenuType.CRAFT_LIST_MENU.get(), CraftListScreen::new);
        event.register(MaidBakeriesMenuType.CRAFT_ORDER_MENU.get(), CraftOrderScreen::new);
    }
}
