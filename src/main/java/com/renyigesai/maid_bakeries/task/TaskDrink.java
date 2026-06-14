package com.renyigesai.maid_bakeries.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.renyigesai.bakeries.init.BakeriesItems;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.entity.task.common.MaidDrinkMoveTask;
import com.renyigesai.maid_bakeries.entity.task.common.MaidDrinkTask;
import com.renyigesai.maid_bakeries.init.MaidBakeriesItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class TaskDrink implements IMaidTask {
    public static final ResourceLocation UID = new ResourceLocation(MaidBakeries.MODID, "drink");
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(BakeriesItems.ICED_LATTE.get());
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid entityMaid) {
        return null;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid entityMaid) {
        MaidDrinkMoveTask move = new MaidDrinkMoveTask(0.6f,4);
        MaidDrinkTask task = new MaidDrinkTask(2);
        return Lists.newArrayList(Pair.of(5, move), Pair.of(6, task));
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Lists.newArrayList(Pair.of("has_drink_sticky_note", this::hasDrinkStickyNote));
    }

    private boolean hasDrinkStickyNote(EntityMaid maid) {
        return maid.getMainHandItem().is(MaidBakeriesItems.DRINK_STICKY_NOTE.get());
    }
}
