package com.renyigesai.maid_bakeries.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.renyigesai.bakeries.common.init.BakeriesItems;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.entity.task.common.MaidCutMoveTask;
import com.renyigesai.maid_bakeries.entity.task.common.MaidCutTask;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class TaskCut implements IMaidTask {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(MaidBakeries.MODID, "cut");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return BakeriesItems.BREAD_KNIFE.get().getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid entityMaid) {
        return null;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid entityMaid) {
        MaidCutMoveTask move = new MaidCutMoveTask(0.6f,4);
        MaidCutTask task = new MaidCutTask(2);
        return Lists.newArrayList(Pair.of(5, move), Pair.of(6, task));
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Lists.newArrayList(Pair.of("has_knife", this::hasKnife));
    }

    private boolean hasKnife(EntityMaid maid) {
        return maid.getMainHandItem().is(BakeriesItems.BREAD_KNIFE.get()) || maid.getMainHandItem().is(ItemTags.create(Identifier.fromNamespaceAndPath("c","tools/knives")));
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return false;
    }

    @Override
    public boolean enableEating(EntityMaid maid) {
        return false;
    }

}
