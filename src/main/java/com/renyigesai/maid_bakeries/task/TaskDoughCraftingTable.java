package com.renyigesai.maid_bakeries.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.renyigesai.bakeries.init.BakeriesItems;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.entity.task.common.MaidDoughCraftingTableTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TaskDoughCraftingTable implements IMaidTask {
    public static final ResourceLocation UID = new ResourceLocation(MaidBakeries.MODID, "dough_crafting_table");
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return BakeriesItems.DOUGH_CRAFTING_TABLE.get().getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid entityMaid) {
        return null;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        MaidDoughCraftingTableTask doughCraftingTableTask = new MaidDoughCraftingTableTask(0.5f, 2);
        return Lists.newArrayList(Pair.of(5, doughCraftingTableTask));
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return false;
    }
}
