package com.renyigesai.maid_bakeries.gui;

import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.data.IngredientData;
import com.renyigesai.maid_bakeries.data.RecipeCatalog;
import com.renyigesai.maid_bakeries.network.Messages;
import com.renyigesai.maid_bakeries.network.CheckoutMessage;
import com.renyigesai.maid_bakeries.network.StickyNotePageTurningMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class CraftListScreen extends AbstractContainerScreen<CraftListMenu> {
    public final Player player;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MaidBakeries.MODID, "textures/gui/craft_list.png");
    private static final int[] LINE = new int[]{47,76,105,134,163};
    private static final int BUTTON_SIZE = 16;
    private static final int BUTTON_U = 46;
    private static final int BUTTON_V = 196;
    private final java.util.Map<Integer, Rectangle> buttonBounds = new java.util.HashMap<>();
    public CraftListScreen(CraftListMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
        this.player = p_97742_.player;
        this.imageWidth = 135;
        this.imageHeight = 196;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int page = player.getMainHandItem().getOrCreateTag().getInt("Page");
        int itemsPerPage = 5;
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, RecipeCatalog.items.size());
        int line = 0;
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        buttonBounds.clear();
        for (int i = start; i < end; i++) {
            List<IngredientData> stacks = RecipeCatalog.items.get(i);
            if (!stacks.isEmpty()) {
                int inx = 0;
                for (; inx < stacks.size(); inx++) {
                    String type = stacks.get(inx).type;
                    ItemStack item = stacks.get(inx).stack;
                    if (!item.isEmpty()) {
                        pGuiGraphics.renderItem(item, x + 8 + (inx * 32), y + LINE[line]);
                        placeLogo(pGuiGraphics,type,x + 8 + (inx * 32) + 2, y + LINE[line] - 8);
                    }
                }
                int buttonX = x + imageWidth - 8 - BUTTON_SIZE; // �����ұ߿� 8 ����
                int buttonY = y + LINE[line] - 4; // ��ֱ���У������и�16��
                pGuiGraphics.blit(TEXTURE, buttonX, buttonY, BUTTON_U, BUTTON_V, BUTTON_SIZE, BUTTON_SIZE, 256, 256);
                buttonBounds.put(i, new Rectangle(buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE));
                line++;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // ���ȼ�鰴ť���
        for (var entry : buttonBounds.entrySet()) {
            Rectangle rect = entry.getValue();
            if (rect.contains(mouseX, mouseY)) {
                int rowIndex = entry.getKey();
                List<IngredientData> ingredientData = RecipeCatalog.items.get(rowIndex);
                ItemStackHandler handler = new ItemStackHandler(ingredientData.size());
                AtomicInteger slot = new AtomicInteger();
                StringBuffer string = new StringBuffer();
                ingredientData.forEach(data -> {
                    handler.setStackInSlot(slot.get(),data.stack);
                    slot.getAndIncrement();
                    string.append(data.type);
                    string.append("&");
                });
                Messages.sendToServer(new CheckoutMessage(handler.serializeNBT(),string.toString()));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        int x = this.leftPos;
        int y = this.topPos;
        if (pMouseX >= x && pMouseX <= x + 104 && pMouseY >= y + 26 && pMouseY <= y + 195){
            boolean flag = pDelta == 1.0;
            int page = flag ? -1 : 1;
            ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            int oldPage = itemInHand.getOrCreateTag().getInt("Page");
            if (oldPage == 0 && page == -1){
                return super.mouseScrolled(pMouseX, pMouseY, pDelta);
            }
            itemInHand.getOrCreateTag().putInt("Page",oldPage + (page) );
            Messages.sendToServer(new StickyNotePageTurningMessage(page));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT, 2.0F));
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    public void placeLogo(GuiGraphics guiGraphics,String type,int x,int y){
        if ("oven".equals(type)){
            guiGraphics.blit(TEXTURE,x,y,24,196,10,10,256,256);
        }else if ("dough_crafting_table".equals(type)){
            guiGraphics.blit(TEXTURE,x,y,11,196,12,10,256,256);
        }else if ("blender".equals(type)){
            guiGraphics.blit(TEXTURE,x,y,0,196,10,10,256,256);
        }else {
            guiGraphics.blit(TEXTURE,x,y,35,196,10,10,256,256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {

    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        for (var entry : buttonBounds.entrySet()) {
            Rectangle rect = entry.getValue();
            if (rect.contains(mouseX, mouseY)) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(rect.x - 2, rect.y - 2,0);
                guiGraphics.pose().scale(1.25f,1.25f,1f);
                guiGraphics.blit(TEXTURE, 0, 0, 62, 196, BUTTON_SIZE, BUTTON_SIZE, 256, 256);
                guiGraphics.pose().popPose();
                break;
            }
        }
        int x = this.leftPos;
        int y = this.topPos;
        if (mouseX >= x + 7 && mouseX <= x + 36 && mouseY >= y && mouseY <= y + 26){
            guiGraphics.blit(TEXTURE, x + 7, y, 0, 211, 30, 27, 256, 256);
        }
    }

    public static class PageTurningMessage{
        private final int page;

        public PageTurningMessage(FriendlyByteBuf buffer) {
            this.page = buffer.readInt();
        }

        public PageTurningMessage(int page) {
            this.page = page;
        }

        public static void toBytes(PageTurningMessage message, FriendlyByteBuf buffer) {
            buffer.writeInt(message.page);
        }

        public static void handle(PageTurningMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                Player entity = context.getSender();
                int page = message.page;
                if (entity == null){
                    return;
                }
                handleButtonAction(entity,page);
            });
            context.setPacketHandled(true);
        }

        public static void handleButtonAction(Player entity,int page) {
            ItemStack itemInHand = entity.getItemInHand(InteractionHand.MAIN_HAND);
            int oldPage = itemInHand.getOrCreateTag().getInt("Page");
            if (page < 1 && oldPage == 1){
                return;
            }
            itemInHand.getOrCreateTag().putInt("Page",oldPage + (page) );
        }
    }
}
