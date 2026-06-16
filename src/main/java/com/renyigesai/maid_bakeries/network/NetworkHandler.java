package com.renyigesai.maid_bakeries.network;

import com.renyigesai.bakeries.common.utils.ItemUtils;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.data.RecipeCatalog;
import com.renyigesai.maid_bakeries.init.MaidBakeriesDataComponents;
import com.renyigesai.maid_bakeries.init.MaidBakeriesItems;
import com.renyigesai.maid_bakeries.item.CraftListItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = MaidBakeries.MODID)
public class NetworkHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MaidBakeries.MODID).versioned("1.0");

        registrar.playToServer(
                SyncRepeatPayload.TYPE,
                SyncRepeatPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        ItemStack stack = payload.stack();
                        int wineListPage = stack.getOrDefault(MaidBakeriesDataComponents.INT.get(),0);
                        int newWineListPage = Math.max(wineListPage + payload.repeat(), 0);
                        stack.set(MaidBakeriesDataComponents.INT.get(),newWineListPage);
                    });
                }
        );

        registrar.playToServer(
                SyncCheckoutPayload.TYPE,
                SyncCheckoutPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        List<ItemStack> stacks = payload.stacks();
                        String ids = payload.ids();
                        ItemStack listStack = context.player().getItemInHand(InteractionHand.MAIN_HAND);
                        ItemStack order = new ItemStack(MaidBakeriesItems.CRAFT_ORDER.get());
                        order.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
                        order.set(MaidBakeriesDataComponents.TEXT, ids);
                        ItemUtils.givePlayerItem(context.player(), order);
                        if (listStack.getItem() instanceof CraftListItem){
                            CraftListItem.clear(listStack);
                        }
                        context.player().containerMenu.broadcastChanges();
                    });
                }
        );

        registrar.playToServer(
                SyncCraftListAddPayload.TYPE,
                SyncCraftListAddPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        ServerPlayer player = (ServerPlayer) context.player();
                        ItemStack listItem = player.getMainHandItem();
                        ItemStack stack = payload.stack();
                        String ids = payload.ids();
                        List<ItemStack> stacks = new ArrayList<>();
                        for (int i = 0; i < listItem.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).getSlots(); i++) {
                            stacks.add(listItem.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).getStackInSlot(i));
                        }
                        stacks.add(stack);
                        listItem.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
                        listItem.set(MaidBakeriesDataComponents.TEXT, ids);
                        player.containerMenu.broadcastChanges();
                    });
                }
        );

    }
}