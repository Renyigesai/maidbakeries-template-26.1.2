package com.renyigesai.maid_bakeries.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.renyigesai.bakeries.init.BakeriesItems;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.entity.task.common.MaidOvenTask;
import com.renyigesai.maid_bakeries.init.MaidBakeriesItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class TaskOven implements IMaidTask {
    public static final ResourceLocation UID = new ResourceLocation(MaidBakeries.MODID, "oven");
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return BakeriesItems.OVEN.get().getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid entityMaid) {
        return null;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        MaidOvenTask ovenTask = new MaidOvenTask(0.5f, 2);
        return Lists.newArrayList(Pair.of(5, ovenTask));
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Lists.newArrayList(Pair.of("has_sticky_note", this::hasStickyNote));
    }

    @Override
    public boolean enableEating(EntityMaid maid) {
        return false;
    }

    private boolean hasStickyNote(EntityMaid maid) {
        return maid.getMainHandItem().is(MaidBakeriesItems.OVEN_STICKY_NOTE.get());
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return false;
    }
}
