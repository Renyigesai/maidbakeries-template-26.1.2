package com.renyigesai.maid_bakeries.task;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.util.Comparator;

public abstract class AbstractCraftMaidTask extends MaidCheckRateTask{
    public ItemStack matchingStack;
    public String cacheRecipeId;
    public int targetCount = 1;
    public boolean end;

    public AbstractCraftMaidTask() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
    }

    protected abstract TagKey<PoiType> getPotType();

    public BlockPos findBlock(ServerLevel world, EntityMaid maid){
        BlockPos blockPos = maid.getBrainSearchPos();
        PoiManager poiManager = world.getPoiManager();
        int range = (int) maid.getRestrictRadius();
        return poiManager.getInRange(type -> type.is(getPotType()), blockPos, range, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos).filter(pos -> canCollect(world, pos))
                .min(Comparator.comparingDouble(pos -> pos.distSqr(maid.blockPosition()))).orElse(null);
    }

    public boolean canCollect(ServerLevel world, BlockPos hivePos){
        return true;
    }

    public boolean successFlag(ServerLevel level, EntityMaid maid){
        CombinedInvWrapper maidAvailableInv = maid.getAvailableInv(true);
        if (ItemsUtil.isStackIn(maidAvailableInv, stack -> stack.is(matchingStack.getItem()))){
            ItemStack maidStack = ItemsUtil.getStack(maid, stack -> stack.is(matchingStack.getItem()));
            int x = end ? targetCount : 1;
            return maidStack.getCount() >= matchingStack.getCount() * x;
        }
        return false;
    }

    public void setMatchingStack(ItemStack stack){
        matchingStack = stack;
    }

    public void setCacheRecipeId(String cacheRecipeId) {
        this.cacheRecipeId = cacheRecipeId;
    }
    public abstract boolean isTake(BlockEntity blockEntity);
    public abstract TaskResult put(BlockEntity blockEntity, CombinedInvWrapper maidAvailableInv, EntityMaid maid,ServerLevel level);
    public abstract TaskResult take(BlockEntity blockEntity, CombinedInvWrapper maidAvailableInv,ServerLevel level);

    public abstract void onCraft(ServerLevel level, EntityMaid maid,CombinedInvWrapper maidAvailableInv);

    public abstract boolean notBlockEntity();
}
