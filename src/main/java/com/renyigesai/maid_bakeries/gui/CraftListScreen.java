package com.renyigesai.maid_bakeries.gui;

import com.renyigesai.bakeries.common.init.BakeriesItems;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.init.MaidBakeriesDataComponents;
import com.renyigesai.maid_bakeries.init.MaidBakeriesItems;
import com.renyigesai.maid_bakeries.item.CraftListItem;
import com.renyigesai.maid_bakeries.network.SyncCheckoutPayload;
import com.renyigesai.maid_bakeries.network.SyncCraftListAddPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.IdentifierException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("removal")
public class CraftListScreen extends AbstractContainerScreen<CraftListMenu> {
    public final Player player;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MaidBakeries.MODID, "textures/gui/craft_list.png");
    private static final Identifier TEXTURE_ICON = Identifier.fromNamespaceAndPath(MaidBakeries.MODID, "textures/gui/icons.png");
    public int typePage = 0;
    public static final List<Item> TYPE_ICONS = List.of(BakeriesItems.BLENDER.get(),BakeriesItems.DOUGH_CRAFTING_TABLE.get(),BakeriesItems.OVEN.get(), Items.CRAFTING_TABLE);
    public static final List<String> TYPES = List.of("blender","dough_crafting_table","oven","crafting_shapeless");
    public int typeSize;
    private ItemStack selectStack;
    private int stackCount = 1;
    private EditBox editBox;
    private static final int[] XOS = new int[]{14,32,14,32,14,32};
    private static final int[] YOS = new int[]{95,95,113,113,131,131};
    public CraftListScreen(CraftListMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
        this.player = p_97742_.player;
        typeSize = TYPES.size();
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - 135) / 2;
        int y = (height - 170) / 2;
        editBox = new EditBox(this.font, x + 17, y + 75, 69, 10, Component.literal("name"));
        editBox.setBordered(false);
        editBox.setMaxLength(128);
        editBox.setHint(Component.translatable("container.maid_bakeries.editbox.hint").withColor(0x9a775b).withStyle(ChatFormatting.ITALIC));
        this.addRenderableWidget(editBox);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int x = (width - 135) / 2;
        int y = (height - 170) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE, x, y, 0, 0, 135, 170,256,256);

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 56, y + 22);
        graphics.pose().scale(1.5f, 1.5f);
        graphics.fakeItem(new ItemStack(TYPE_ICONS.get(this.typePage)), 0, 0);
        graphics.pose().popMatrix();


        String itemId = this.editBox.getValue();
        if (itemId != null && !itemId.isEmpty()){
            try {
                Identifier parse = Identifier.parse(itemId);
                Optional<Holder.Reference<Item>> itemReference = BuiltInRegistries.ITEM.get(ResourceKey.create(Registries.ITEM, parse));
                if (itemReference.isPresent()){
                    this.selectStack = new ItemStack(itemReference.get().value(),this.stackCount);
                    graphics.fakeItem(this.selectStack,x + 93,y + 71);
                }
            }catch (IdentifierException e){
                this.editBox.setValue("");
                MaidBakeries.LOGGER.error(e);
            }
        }

        placeLogo(graphics,this.typePage,x + 44,y + 43);
        if (mouseX >= x + 111 && mouseX <= x + 121 && mouseY >= y + 75 && mouseY <= y + 85){
            graphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,x + 111,y + 75,11,170,11,11,256,256);
        }else {
            graphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,x + 111,y + 75,0,170,11,11,256,256);
        }

        if (mouseX >= x + 109 && mouseX <= x + 121 && mouseY >= y + 115 && mouseY <= y + 127){
            graphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,x + 109 + 3,y + 115 + 3,38,170,16,16,256,256);
        }else {
            graphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,x + 109 + 3,y + 115 + 3,22,170,16,16,256,256);
        }

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() instanceof CraftListItem){
            List<ItemStack> list = CraftListItem.getInventoryList(mainHandItem);
            if (!list.isEmpty()){
                for (int i = 0; i < list.size(); i++) {
                    ItemStack stack0 = list.get(i);
                    if (!stack0.isEmpty()){
                        graphics.fakeItem(stack0,x + XOS[i],y  + YOS[i]);
                    }
                }
            }
        }
        addTooltip(graphics,mouseX,mouseY,x,y);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double scrollX, double scrollY) {
        int x = (width - 135) / 2;
        int y = (height - 170) / 2;
        boolean flag = scrollY > 0;
        int roller = flag ? -1 : 1;
        boolean isScrolled = addTypeScrolled(pMouseX,pMouseY,x,y,roller) || addSelectStackCountScrolled(pMouseX,pMouseY,x,y,roller);
        if (isScrolled){
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT, 2.0F));
        }
        return super.mouseScrolled(pMouseX, pMouseY, scrollX, scrollY) || isScrolled;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = (width - 135) / 2;
        int y = (height - 170) / 2;
        double mouseX = event.x();
        double mouseY = event.y();
        boolean isClicked = addSelectStackClicked(mouseX,mouseY,x,y) ||  addCheckoutClicked(mouseX,mouseY,x,y);
        if (isClicked){
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F));
        }
        return super.mouseClicked(event, doubleClick) || isClicked;
    }

    public void placeLogo(GuiGraphicsExtractor guiGraphics, int type, int x, int y){
        switch (type){
            case 0 -> guiGraphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE_ICON,x,y,0,0,10,10,64,16);
            case 1 -> guiGraphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE_ICON,x,y,11,0,12,10,64,16);
            case 2 -> guiGraphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE_ICON,x,y,24,0,10,10,64,16);
            case 3 -> guiGraphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE_ICON,x,y,35,0,10,10,64,16);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {

    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.editBox.keyPressed(event)) {
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_E) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.editBox.charTyped(event)) {
            return true;
        }
        if (event.codepointAsString().equals("e") || event.codepointAsString().equals("E")) {
            return true;
        }
        return super.charTyped(event);
    }

    private void addTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY,int x,int y){
        if (mouseX >= x + 56 && mouseX <= x + 78 && mouseY >= y + 23 && mouseY <= y + 44){
            Component message = Component.translatable("container.maid_bakeries.craft_type." + TYPES.get(typePage)).withStyle(ChatFormatting.GOLD).append(" ").append(Component.translatable("container.maid_bakeries.craft_type").withStyle(ChatFormatting.WHITE));
            List<Component> tooltip = List.of(
                    message,
                    Component.translatable("container.maid_bakeries.rolling").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC)
            );
            graphics.setTooltipForNextFrame(this.font,tooltip,Optional.empty(),mouseX,mouseY);
        }
        if (this.selectStack != null && !this.selectStack.isEmpty() && mouseX >= x + 94 && mouseX <= x + 107 && mouseY >= y + 74 && mouseY <= y + 86){
            List<Component> tooltip = List.of(
                    this.selectStack.getItemName(),
                    Component.translatable("container.maid_bakeries.requirement_count").withStyle(ChatFormatting.WHITE).append(Component.literal(String.valueOf(this.selectStack.count())).withStyle(ChatFormatting.GOLD)),
                    Component.translatable("container.maid_bakeries.rolling_requirement_count").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC)
            );
            graphics.setTooltipForNextFrame(this.font,tooltip,Optional.empty(),mouseX,mouseY);
        }
    }

    private boolean addTypeScrolled(double pMouseX, double pMouseY,int x,int y,int roller){
        if (pMouseX >= x + 56 && pMouseX <= x + 78 && pMouseY >= y + 23 && pMouseY <= y + 44){
            int newTypePage = this.typePage + roller;
            if (newTypePage >= this.typeSize){
                this.typePage = 0;
            }else if (newTypePage < 0){
                this.typePage = this.typeSize - 1;
            } else {
                this.typePage = newTypePage;
            }
            return true;
        }
        return false;
    }

    private boolean addSelectStackCountScrolled(double pMouseX, double pMouseY,int x,int y,int roller){
        if (this.selectStack != null && !this.selectStack.isEmpty() && pMouseX >= x + 94 && pMouseX <= x + 107 && pMouseY >= y + 74 && pMouseY <= y + 86) {
            int count = this.selectStack.getCount();
            int newCount = count + roller;
            if (newCount > 64) {
                this.stackCount = 1;
            } else if (newCount < 1) {
                this.stackCount = 64;
            } else {
                this.stackCount = newCount;
            }
            this.selectStack.setCount(this.stackCount);
            return true;
        }
        return false;
    }

    private boolean addSelectStackClicked(double mouseX, double mouseY, int x, int y){
        if (mouseX >= x + 111 && mouseX <= x + 121 && mouseY >= y + 75 && mouseY <= y + 85){
            if (this.selectStack != null && !this.selectStack.isEmpty()){
                ItemStack listItem = player.getMainHandItem();
                String ids = TYPES.get(this.typePage);
                String existing = listItem.getOrDefault(MaidBakeriesDataComponents.TEXT, "");
                String newIds;
                if (existing.isEmpty()) {
                    newIds = "&" + ids;
                } else {
                    List<String> existingList = Arrays.asList(existing.split("&"));
                    if (!existingList.contains(ids)) {
                        newIds = existing + "&" + ids;
                    } else {
                        newIds = existing;
                    }
                }
                List<ItemStack> stacks = new ArrayList<>();
                for (int i = 0; i < listItem.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).getSlots(); i++) {
                    stacks.add(listItem.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).getStackInSlot(i));
                }
                stacks.add(this.selectStack);
                listItem.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
                listItem.set(MaidBakeriesDataComponents.TEXT, newIds);
                ClientPacketDistributor.sendToServer(new SyncCraftListAddPayload(this.selectStack,listItem,newIds));
                this.editBox.setValue("");
                return true;
            }
        }
        return false;
    }

    private boolean addCheckoutClicked(double mouseX, double mouseY, int x, int y){
        if (mouseX >= x + 109 && mouseX <= x + 121 && mouseY >= y + 115 && mouseY <= y + 127){
            ItemStack listItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (listItem.is(MaidBakeriesItems.CRAFT_LIST)){
                List<ItemStack> stacks = new ArrayList<>();
                for (int i = 0; i < listItem.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).getSlots(); i++) {
                    stacks.add(listItem.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).getStackInSlot(i));
                }
                String ids = listItem.getOrDefault(MaidBakeriesDataComponents.TEXT, "");
                ClientPacketDistributor.sendToServer(new SyncCheckoutPayload(stacks,ids,listItem));
                if (listItem.getItem() instanceof CraftListItem){
                    CraftListItem.clear(listItem);
                }
                return true;
            }
        }
        return false;
    }

}
