package com.renyigesai.maid_bakeries.network;


import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.gui.CraftListScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Messages {

    public static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(MaidBakeries.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        INSTANCE = net;
        net.messageBuilder(CraftListScreen.PageTurningMessage.class, id(), NetworkDirection.PLAY_TO_SERVER).decoder(CraftListScreen.PageTurningMessage::new).encoder(CraftListScreen.PageTurningMessage::toBytes).consumerMainThread(CraftListScreen.PageTurningMessage::handle).add();
        net.messageBuilder(StickyNotePageTurningMessage.class, id(), NetworkDirection.PLAY_TO_SERVER).decoder(StickyNotePageTurningMessage::new).encoder(StickyNotePageTurningMessage::toBytes).consumerMainThread(StickyNotePageTurningMessage::handle).add();
        net.messageBuilder(CheckoutMessage.class, id(), NetworkDirection.PLAY_TO_SERVER).decoder(CheckoutMessage::new).encoder(CheckoutMessage::toBytes).consumerMainThread(CheckoutMessage::handle).add();
        net.messageBuilder(SyncRecipeCatalogMessage.class, id(), NetworkDirection.PLAY_TO_CLIENT).decoder(SyncRecipeCatalogMessage::new).encoder(SyncRecipeCatalogMessage::toBytes).consumerMainThread(SyncRecipeCatalogMessage::handle).add();
        net.messageBuilder(SyncRepeatMessage.class, id(), NetworkDirection.PLAY_TO_SERVER).decoder(SyncRepeatMessage::new).encoder(SyncRepeatMessage::toBytes).consumerMainThread(SyncRepeatMessage::handle).add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);

    }
    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <MSG> void sendToPlayersTrackingEntity(MSG message, Entity entity) {
        sendToPlayersTrackingEntity(message, entity, false);
    }

    public static <MSG> void sendToPlayersTrackingEntity(MSG message, Entity entity, boolean sendToSource) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
        if (sendToSource && entity instanceof ServerPlayer serverPlayer)
            sendToPlayer(message, serverPlayer);
    }
}