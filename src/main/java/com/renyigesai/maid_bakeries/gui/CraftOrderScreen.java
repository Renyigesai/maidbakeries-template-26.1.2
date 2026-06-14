package com.renyigesai.maid_bakeries.gui;

import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.item.CraftOrderItem;
import com.renyigesai.maid_bakeries.network.Messages;
import com.renyigesai.maid_bakeries.network.StickyNotePageTurningMessage;
import com.renyigesai.maid_bakeries.network.SyncRepeatMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CraftOrderScreen extends AbstractContainerScreen<CraftOrderMenu> {
    public final Player player;
    private static final ResourceLocation TEXTURE;
    private static final Map<String,ResourceLocation> ICONS;
    private float prevOffsetX = 0, prevOffsetY = 0;
    private float targetOffsetX = 0, targetOffsetY = 0;
    public ItemStack endStack = ItemStack.EMPTY;
    public List<ItemStack> inventoryList = new ArrayList<>();
    public CraftOrderScreen(CraftOrderMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.player = pPlayerInventory.player;
        this.imageWidth = 139;
        this.imageHeight = 153;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        float[] offset = getOffset(pMouseX, pMouseY, 5, v);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(offset[0],offset[1],0);
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.getItem() instanceof CraftOrderItem){
            int centerX = x + 54;
            int centerY = y + 18;
            ItemStack end = getEndStack(itemInHand);
            Minecraft mc = Minecraft.getInstance();
            if (!end.isEmpty()){
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(centerX, centerY, 0);
                guiGraphics.pose().scale(2,2,1);
                guiGraphics.renderFakeItem(end,0,0);
                guiGraphics.pose().popPose();
                String key = end.getItem().getDescriptionId();
                int repeat = player.getMainHandItem().getOrCreateTag().getInt("Repeat");
                Component nameComponent;
                if (repeat > 1){
                    nameComponent = Component.translatable(key).append("x").append(String.valueOf(repeat)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.ITALIC);
                }else {
                    nameComponent = Component.translatable(key).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.ITALIC);
                }
                int textWidth = this.font.width(nameComponent);
                guiGraphics.drawString(mc.font, nameComponent,centerX + 16 - textWidth / 2, centerY + 32,4210752, false);
            }
            List<ItemStack> stacks = getInventoryList(itemInHand);
            for (int i = 0; i < stacks.size(); i++) {
                guiGraphics.renderFakeItem(stacks.get(i),x + 34 + (i * 24),y + 97);
            }
            List<String> types = CraftOrderItem.getTypes(itemInHand);
            for (int i = 0; i < types.size(); i++) {
                guiGraphics.blit(ICONS.get(types.get(i)),x + 34 + (i * 24),y + 113,0,0,15,15,16,16);
            }
        }
        guiGraphics.pose().popPose();
        prevOffsetX = targetOffsetX;
        prevOffsetY = targetOffsetY;

    }
    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {

    }

    public float[] getOffset(int pMouseX,int pMouseY,int maxOffset,float partialTick){
        int centerX = width / 2;
        int centerY = height / 2;
        float tx = (float) (centerX - pMouseX) / ((float) width / 2);
        float ty = (float) (centerY - pMouseY) / ((float) height / 2);
        tx = Math.max(-1f, Math.min(1f, tx));
        ty = Math.max(-1f, Math.min(1f, ty));
        float curvedTx = (float) Math.sqrt(Math.abs(tx)) * Math.signum(tx);
        float curvedTy = (float) Math.sqrt(Math.abs(ty)) * Math.signum(ty);
        targetOffsetX = curvedTx * maxOffset;
        targetOffsetY = curvedTy * maxOffset;
        float renderOffsetX = prevOffsetX + (targetOffsetX - prevOffsetX) * partialTick;
        float renderOffsetY = prevOffsetY + (targetOffsetY - prevOffsetY) * partialTick;
        return new float[]{renderOffsetX,renderOffsetY};
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        int x = this.leftPos;
        int y = this.topPos;
        if (pMouseX >= x && pMouseX <= x + 138 && pMouseY >= y && pMouseY <= y + 151){
            boolean flag = pDelta == 1.0;
            int repeat = flag ? 1 : -1;
            ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            int oldRepeat = itemInHand.getOrCreateTag().getInt("Repeat");
            if (oldRepeat == 64 || oldRepeat == 1 && repeat == -1){
                return super.mouseScrolled(pMouseX, pMouseY, pDelta);
            }
            itemInHand.getOrCreateTag().putInt("Repeat",oldRepeat + (repeat));
            Messages.sendToServer(new SyncRepeatMessage(repeat));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT, 2.0F));
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    private ItemStack getEndStack(ItemStack itemInHand){
        if (endStack.isEmpty()){
            endStack = CraftOrderItem.getEnd(itemInHand);
            return endStack;
        }
        return endStack;
    }

    private List<ItemStack> getInventoryList(ItemStack itemInHand){
        if (inventoryList.isEmpty()){
            inventoryList = CraftOrderItem.getInventoryList(itemInHand);
            return inventoryList;
        }
        return inventoryList;
    }

    static {
        TEXTURE = ResourceLocation.fromNamespaceAndPath(MaidBakeries.MODID, "textures/gui/craft_order.png");
        ICONS = Map.of(
                "blender",ResourceLocation.fromNamespaceAndPath(MaidBakeries.MODID,"textures/gui/blender_icon.png"),
                "dough_crafting_table",ResourceLocation.fromNamespaceAndPath(MaidBakeries.MODID,"textures/gui/dough_crafting_table_icon.png"),
                "oven",ResourceLocation.fromNamespaceAndPath(MaidBakeries.MODID,"textures/gui/oven_icon.png"),
                "crafting",ResourceLocation.fromNamespaceAndPath(MaidBakeries.MODID,"textures/gui/crafting_icon.png"));
    }

}
