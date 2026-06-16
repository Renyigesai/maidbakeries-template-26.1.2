package com.renyigesai.maid_bakeries.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.renyigesai.bakeries.common.init.BakeriesItems;
import com.renyigesai.maid_bakeries.MaidBakeries;
import com.renyigesai.maid_bakeries.entity.task.impl.MaidBakingTask;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TaskBaking implements IMaidTask {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(MaidBakeries.MODID, "baking");
    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(BakeriesItems.BAGEL.get());
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.environmentSound(maid, (SoundEvent) InitSounds.MAID_FURNACE.get(), 0.5F);
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid entityMaid) {
        return Lists.newArrayList(Pair.of(5, new MaidBakingTask(0.5f, 2)));
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return false;
    }
}
