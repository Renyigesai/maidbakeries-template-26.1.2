package com.renyigesai.maid_bakeries.gui;

import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.init.MaidBakeriesDataComponents;
import com.renyigesai.maid_bakeries.item.CraftOrderItem;
import com.renyigesai.maid_bakeries.network.SyncRepeatPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CraftOrderScreen extends AbstractContainerScreen<CraftOrderMenu> {
    public final Player player;
    private static final Identifier TEXTURE;
    private static final Map<String,Identifier> ICONS;
    private float prevOffsetX = 0, prevOffsetY = 0;
    private float targetOffsetX = 0, targetOffsetY = 0;
    public ItemStack endStack = ItemStack.EMPTY;
    public List<ItemStack> inventoryList = new ArrayList<>();
    public CraftOrderScreen(CraftOrderMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.player = pPlayerInventory.player;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int pMouseX, int pMouseY, float v) {
        super.extractBackground(guiGraphics, pMouseX, pMouseY, v);
        int x = (width - 139) / 2;
        int y = (height - 153) / 2;
        float[] offset = getOffset(pMouseX, pMouseY, 5, v);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(offset[0],offset[1]);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE, x, y, 0, 0, imageWidth, imageHeight,256,245);
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.getItem() instanceof CraftOrderItem){
            int centerX = x + 54;
            int centerY = y + 18;
            ItemStack end = getEndStack(itemInHand);
            Minecraft mc = Minecraft.getInstance();
            if (!end.isEmpty()){
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().translate(centerX, centerY);
                guiGraphics.pose().scale(2,2);
                guiGraphics.fakeItem(end,0,0);
                guiGraphics.pose().popMatrix();
                String key = end.getItem().getDescriptionId();
                int repeat = end.count();
                Component nameComponent;
                if (repeat > 1){
                    nameComponent = Component.translatable(key).append("x").append(String.valueOf(repeat)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.ITALIC);
                }else {
                    nameComponent = Component.translatable(key).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.ITALIC);
                }
                int textWidth = this.font.width(nameComponent);
                guiGraphics.text(mc.font, nameComponent,centerX + 16 - textWidth / 2, centerY + 32,-12566464, false);
            }
            List<ItemStack> stacks = getInventoryList(itemInHand);
            for (int i = 0; i < stacks.size(); i++) {
                guiGraphics.fakeItem(stacks.get(i),x + 34 + (i * 24),y + 97);
            }
            List<String> types = CraftOrderItem.getTypes(itemInHand);
            for (int i = 0; i < types.size(); i++) {
                Identifier identifier = ICONS.get(types.get(i));
                if (identifier != null){
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED,identifier,x + 34 + (i * 24),y + 113,0,0,15,15,16,16);
                }

            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {

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
    public boolean mouseScrolled(double pMouseX, double pMouseY, double scrollX, double scrollY) {
        int x = this.leftPos;
        int y = this.topPos;
        if (pMouseX >= x && pMouseX <= x + 138 && pMouseY >= y && pMouseY <= y + 151){
            boolean flag = scrollY > 0;
            int repeat = flag ? -1 : 1;
            ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            int oldRepeat = itemInHand.getOrDefault(MaidBakeriesDataComponents.INT.get(),0);
            if (oldRepeat == 64 || oldRepeat == 1 && repeat == -1){
                return super.mouseScrolled(pMouseX, pMouseY, scrollX,scrollY);
            }
            ClientPacketDistributor.sendToServer(new SyncRepeatPayload(itemInHand,repeat));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT, 2.0F));
        }
        return super.mouseScrolled(pMouseX, pMouseY, scrollX, scrollY);
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
        TEXTURE = Identifier.fromNamespaceAndPath(MaidBakeries.MODID, "textures/gui/craft_order.png");
        ICONS = Map.of(
                "blender",Identifier.fromNamespaceAndPath(MaidBakeries.MODID,"textures/gui/blender_icon.png"),
                "dough_crafting_table",Identifier.fromNamespaceAndPath(MaidBakeries.MODID,"textures/gui/dough_crafting_table_icon.png"),
                "oven",Identifier.fromNamespaceAndPath(MaidBakeries.MODID,"textures/gui/oven_icon.png"),
                "crafting_shapeless",Identifier.fromNamespaceAndPath(MaidBakeries.MODID,"textures/gui/crafting_icon.png"));
    }

}
