package com.renyigesai.maid_bakeries.item;

import com.renyigesai.maid_bakeries.data.RecipeCatalog;
import com.renyigesai.maid_bakeries.gui.CraftListMenu;
import com.renyigesai.maid_bakeries.network.Messages;
import com.renyigesai.maid_bakeries.network.SyncRecipeCatalogMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class CraftListItem extends Item {
    public CraftListItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.is(this)) {
            player.startUsingItem(hand);
            if (level instanceof ServerLevel serverLevel){
                RecipeCatalog.init(serverLevel);
//                Messages.sendToServer(new SyncRecipeCatalogPacket(RecipeCatalog.items));
                Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SyncRecipeCatalogMessage(RecipeCatalog.items));
            }
            if (!itemInHand.getOrCreateTag().contains("Page")) {
                itemInHand.getOrCreateTag().putInt("Page", 0);
            }
            player.openMenu(getMenuProvider());
            return new InteractionResultHolder(InteractionResult.SUCCESS, player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider((p_53812_, p_53813_, p_53814_) -> new CraftListMenu(p_53812_,p_53813_), Component.nullToEmpty("Craft List"));
    }
}
